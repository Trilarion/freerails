/*
 * Created on 18-Feb-2005
 *
 */
package freerails.controller;

import freerails.move.*;
import freerails.world.common.*;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.player.Player;
import freerails.world.station.StationModel;
import freerails.world.top.KEY;
import freerails.world.top.ReadOnlyWorld;
import freerails.world.top.WorldDiffs;
import freerails.world.train.*;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static freerails.world.train.SpeedTimeAndStatus.TrainActivity.STOPPED_AT_STATION;
import static freerails.world.train.SpeedTimeAndStatus.TrainActivity.WAITING_FOR_FULL_LOAD;

/**
 * @author Luke
 */
public class TrainStopsHandler implements Serializable {

    private static final Logger logger = Logger
            .getLogger(TrainStopsHandler.class.getName());

    private static final int NOT_AT_STATION = -1;

    private static final long serialVersionUID = 3257567287094882872L;
    private final FreerailsPrincipal principal;
    private final int trainId;
    private final WorldDiffs worldDiffs;
    private GameTime timeLoadingFinished = new GameTime(0);

    public TrainStopsHandler(int id, FreerailsPrincipal p, WorldDiffs w) {
        trainId = id;
        principal = p;
        worldDiffs = w;
    }

    /**
     * If wagons are added to a train, we need to increase its length.
     */
    static PathOnTiles lengthenPath(ReadOnlyWorld w, PathOnTiles path,
                                    int currentTrainLength) {
        double pathDistance = path.getTotalDistance();
        double extraDistanceNeeded = currentTrainLength - pathDistance;

        List<Step> steps = new ArrayList<>();
        ImPoint start = path.getStart();
        Step firstStep = path.getStep(0);
        PositionOnTrack nextPot = PositionOnTrack.createComingFrom(start.x,
                start.y, firstStep);

        while (extraDistanceNeeded > 0) {

            FlatTrackExplorer fte;
            try {
                fte = new FlatTrackExplorer(w, nextPot);
            } catch (NoTrackException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
            fte.nextEdge();
            nextPot.setValuesFromInt(fte.getVertexConnectedByEdge());
            Step cameFrom = nextPot.facing();
            steps.add(0, cameFrom);
            extraDistanceNeeded -= cameFrom.getLength();

        }

        // Add existing steps
        for (int i = 0; i < path.steps(); i++) {
            Step step = path.getStep(i);
            steps.add(step);
        }

        ImPoint newStart = new ImPoint(nextPot.getX(), nextPot.getY());
        path = new PathOnTiles(newStart, steps);
        return path;
    }

    public ImPoint arrivesAtPoint(int x, int y) {
        TrainAccessor ta = new TrainAccessor(worldDiffs, principal, trainId);

        ImPoint targetPoint = ta.getTarget();

        if (x == targetPoint.x && y == targetPoint.y) {
            updateTarget();
            targetPoint = ta.getTarget();
        } else {
            int stationNumber = getStationID(x, y);
            if (NOT_AT_STATION != stationNumber) {
                loadAndUnloadCargo(stationNumber, false, false);
            }
        }
        return targetPoint;
    }

    public Move getMoves() {
        Move m = WorldDiffMove.generate(worldDiffs,
                WorldDiffMove.Cause.TrainArrives);
        worldDiffs.reset();
        return m;
    }

    /**
     * @return the number of the station the train is currently at, or -1 if no
     * current station.
     */
    public int getStationID(int x, int y) {
        // loop thru the station list to check if train is at the same Point
        // as
        // a station
        for (int i = 0; i < worldDiffs.size(principal, KEY.STATIONS); i++) {
            StationModel tempPoint = (StationModel) worldDiffs.get(principal,
                    KEY.STATIONS, i);

            if (null != tempPoint && (x == tempPoint.x) && (y == tempPoint.y)) {
                return i; // train is at the station at location tempPoint
            }
        }

        return -1;
        // there are no stations that exist where the train is currently
    }

    public int getTrainLength() {
        TrainAccessor ta = new TrainAccessor(worldDiffs, principal, trainId);
        return ta.getTrain().getLength();
    }

    public boolean isTrainFull() {
        TrainAccessor train = new TrainAccessor(worldDiffs, principal, trainId);
        ImInts spaceAvailable = train.spaceAvailable();
        return spaceAvailable.sum() == 0;
    }

    public boolean isTrainMoving() {
        if (refreshWaitingForFullLoad()) {
            return false;
        }
        GameTime time = worldDiffs.currentTime();

        return time.getTicks() > this.timeLoadingFinished.getTicks();
    }

    public boolean isWaiting4FullLoad() {
        TrainModel train = (TrainModel) worldDiffs.get(principal, KEY.TRAINS,
                this.trainId);
        int scheduleID = train.getScheduleID();
        ImmutableSchedule schedule = (ImmutableSchedule) worldDiffs.get(
                principal, KEY.TRAIN_SCHEDULES, scheduleID);
        int orderToGoto = schedule.getOrderToGoto();
        if (orderToGoto < 0) {
            return false;
        }
        TrainOrdersModel order = schedule.getOrder(orderToGoto);
        return !isTrainFull() && order.waitUntilFull;
    }

    void loadAndUnloadCargo(int stationId, boolean waiting, boolean autoConsist) {

        // train is at a station so do the cargo processing
        DropOffAndPickupCargoMoveGenerator transfer = new DropOffAndPickupCargoMoveGenerator(
                trainId, stationId, worldDiffs, principal, waiting, autoConsist);
        Move m = transfer.generateMove();
        if (null != m) {
            MoveStatus ms = m.doMove(worldDiffs, principal);
            if (!ms.ok)
                throw new IllegalStateException(ms.message);
        }

    }

    void makeTrainWait(int ticks) {
        GameTime currentTime = worldDiffs.currentTime();
        timeLoadingFinished = new GameTime(currentTime.getTicks() + ticks);
    }

    public boolean refreshWaitingForFullLoad() {

        TrainAccessor ta = new TrainAccessor(worldDiffs, principal, trainId);
        ImmutableSchedule schedule = ta.getSchedule();

        int stationId = ta.getStationId(Double.MAX_VALUE);
        if (stationId < 0)
            throw new IllegalStateException();

        // The train's orders may have changed...
        TrainOrdersModel order = schedule.getOrder(schedule.getOrderToGoto());

        // Should we go to another station?
        if (stationId != order.stationId) {
            return false;
        }

        // Should we change the consist?
        ImInts consist = ta.getTrain().getConsist();
        if (!consist.equals(order.consist)) {
            // ..if so, we should change the consist.
            int oldLength = ta.getTrain().getLength();
            int engineType = ta.getTrain().getEngineType();
            TrainModel newTrain = ta.getTrain().getNewInstance(engineType,
                    order.consist);
            worldDiffs.set(principal, KEY.TRAINS, trainId, newTrain);
            int newLength = newTrain.getLength();
            // has the trains length increased?
            if (newLength > oldLength) {
                TrainMotion tm = ta.findCurrentMotion(Double.MAX_VALUE);
                PathOnTiles path = tm.getPath();
                path = lengthenPath(worldDiffs, path, oldLength);
                SpeedTimeAndStatus.TrainActivity status = isWaiting4FullLoad() ? WAITING_FOR_FULL_LOAD
                        : STOPPED_AT_STATION;
                TrainMotion nextMotion = new TrainMotion(path, newLength, 0,
                        status);

                // Create a new Move object.
                Move trainMove = new NextActivityMove(nextMotion, trainId,
                        principal);
                MoveStatus ms = trainMove.doMove(worldDiffs,
                        Player.AUTHORITATIVE);
                if (!ms.ok)
                    throw new IllegalStateException(ms.message);
            }
        }

        /* Add any cargo that is waiting. */
        loadAndUnloadCargo(schedule.getStationToGoto(), order.waitUntilFull,
                order.autoConsist);

        // Should we stop waiting?
        if (!order.waitUntilFull) {
            updateSchedule();
            return false;
        }

        if (isTrainFull()) {
            updateSchedule();
            return false;
        }

        return true;
    }

    private void scheduledStop() {

        TrainModel train = (TrainModel) worldDiffs.get(principal, KEY.TRAINS,
                this.trainId);
        Schedule schedule = (ImmutableSchedule) worldDiffs.get(principal,
                KEY.TRAIN_SCHEDULES, train.getScheduleID());

        ImInts wagonsToAdd = schedule.getWagonsToAdd();

        // Loading and unloading cargo takes time, so we make the train wait for
        // a few ticks.
        makeTrainWait(50);

        boolean autoConsist = schedule.autoConsist();

        if (null != wagonsToAdd) {
            int engine = train.getEngineType();
            Move m = ChangeTrainMove.generateMove(this.trainId, train, engine,
                    wagonsToAdd, principal);
            m.doMove(worldDiffs, principal);
        }
        updateSchedule();
        int stationToGoto = schedule.getStationToGoto();
        loadAndUnloadCargo(stationToGoto, true, autoConsist);
    }

    void updateSchedule() {
        TrainModel train = (TrainModel) worldDiffs.get(principal, KEY.TRAINS,
                this.trainId);
        int scheduleID = train.getScheduleID();
        ImmutableSchedule currentSchedule = (ImmutableSchedule) worldDiffs.get(
                principal, KEY.TRAIN_SCHEDULES, scheduleID);
        MutableSchedule schedule = new MutableSchedule(currentSchedule);
        StationModel station = null;

        TrainOrdersModel order = schedule.getOrder(schedule.getOrderToGoto());
        boolean waiting4FullLoad = order.waitUntilFull && !isTrainFull();

        if (!waiting4FullLoad) {
            schedule.gotoNextStaton();

            ImmutableSchedule newSchedule = schedule.toImmutableSchedule();
            worldDiffs.set(principal, KEY.TRAIN_SCHEDULES, scheduleID,
                    newSchedule);

            int stationNumber = schedule.getStationToGoto();
            station = (StationModel) worldDiffs.get(principal, KEY.STATIONS,
                    stationNumber);

            if (null == station) {
                logger.warn("null == station, train " + trainId
                        + " doesn't know where to go next!");
            }
        }
    }

    /**
     * Issues a ChangeTrainScheduleMove to set the train to move to the next
     * station.
     */
    public void updateTarget() {
        scheduledStop();
    }

}
