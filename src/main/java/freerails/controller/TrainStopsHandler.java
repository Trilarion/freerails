/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 *
 */
package freerails.controller;

import freerails.move.*;
import freerails.util.ImmutableList;
import freerails.util.Point2D;
import freerails.util.Utils;
import freerails.world.*;
import freerails.world.game.GameTime;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.player.Player;
import freerails.world.station.Station;
import freerails.world.terrain.TileTransition;
import freerails.world.train.*;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
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

    /**
     * @param id
     * @param p
     * @param w
     */
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

        List<TileTransition> tileTransitions = new ArrayList<>();
        Point2D start = path.getStart();
        TileTransition firstTileTransition = path.getStep(0);
        PositionOnTrack nextPot = PositionOnTrack.createComingFrom(start.x,
                start.y, firstTileTransition);

        while (extraDistanceNeeded > 0) {

            FlatTrackExplorer fte;
            try {
                fte = new FlatTrackExplorer(w, nextPot);
            } catch (NoTrackException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
            fte.nextEdge();
            nextPot.setValuesFromInt(fte.getVertexConnectedByEdge());
            TileTransition cameFrom = nextPot.facing();
            tileTransitions.add(0, cameFrom);
            extraDistanceNeeded -= cameFrom.getLength();

        }

        // Add existing tileTransitions
        for (int i = 0; i < path.steps(); i++) {
            TileTransition tileTransition = path.getStep(i);
            tileTransitions.add(tileTransition);
        }

        Point2D newStart = new Point2D(nextPot.getX(), nextPot.getY());
        path = new PathOnTiles(newStart, tileTransitions);
        return path;
    }

    /**
     * @param x
     * @param y
     * @return
     */
    public Point2D arrivesAtPoint(int x, int y) {
        TrainAccessor ta = new TrainAccessor(worldDiffs, principal, trainId);

        Point2D targetPoint = ta.getTarget();

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

    /**
     * @return
     */
    public Move getMoves() {
        Move m = WorldDiffMove.generate(worldDiffs,
                WorldDiffMove.Cause.TrainArrives);
        worldDiffs.reset();
        return m;
    }

    /**
     * @param x
     * @param y
     * @return the number of the station the train is currently at, or -1 if no
     * current station.
     */
    public int getStationID(int x, int y) {
        // loop through the station list to check if train is at the same Point2D as a station
        for (int i = 0; i < worldDiffs.size(principal, KEY.STATIONS); i++) {
            Station tempPoint = (Station) worldDiffs.get(principal,
                    KEY.STATIONS, i);

            if (null != tempPoint && (x == tempPoint.x) && (y == tempPoint.y)) {
                return i; // train is at the station at location tempPoint
            }
        }

        return -1;
        // there are no stations that exist where the train is currently
    }

    /**
     * @return
     */
    public int getTrainLength() {
        TrainAccessor ta = new TrainAccessor(worldDiffs, principal, trainId);
        return ta.getTrain().getLength();
    }

    /**
     * @return
     */
    public boolean isTrainFull() {
        TrainAccessor train = new TrainAccessor(worldDiffs, principal, trainId);
        ImmutableList<Integer> spaceAvailable = train.spaceAvailable();
        return Utils.sumOfIntegerImmutableList(spaceAvailable) == 0;
    }

    /**
     * @return
     */
    public boolean isTrainMoving() {
        if (refreshWaitingForFullLoad()) {
            return false;
        }
        GameTime time = worldDiffs.currentTime();

        return time.getTicks() > this.timeLoadingFinished.getTicks();
    }

    /**
     * @return
     */
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

    /**
     * @return
     */
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
        ImmutableList<Integer> consist = ta.getTrain().getConsist();
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
                TrainActivity status = isWaiting4FullLoad() ? TrainActivity.WAITING_FOR_FULL_LOAD
                        : TrainActivity.STOPPED_AT_STATION;
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

        ImmutableList<Integer> wagonsToAdd = schedule.getWagonsToAdd();

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
        Station station;

        TrainOrdersModel order = schedule.getOrder(schedule.getOrderToGoto());
        boolean waiting4FullLoad = order.waitUntilFull && !isTrainFull();

        if (!waiting4FullLoad) {
            schedule.gotoNextStation();

            ImmutableSchedule newSchedule = schedule.toImmutableSchedule();
            worldDiffs.set(principal, KEY.TRAIN_SCHEDULES, scheduleID,
                    newSchedule);

            int stationNumber = schedule.getStationToGoto();
            station = (Station) worldDiffs.get(principal, KEY.STATIONS,
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
