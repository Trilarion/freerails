package jfreerails.server;

import java.awt.Point;
import java.util.Vector;
import java.util.logging.Logger;
import jfreerails.controller.pathfinder.FlatTrackExplorer;
import jfreerails.controller.pathfinder.SimpleAStarPathFinder;
import jfreerails.move.ChangeTrainMove;
import jfreerails.move.ChangeTrainScheduleMove;
import jfreerails.move.CompositeMove;
import jfreerails.move.Move;
import jfreerails.network.MoveReceiver;
import jfreerails.util.FreerailsIntIterator;
import jfreerails.world.cargo.ImmutableCargoBundle;
import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.GameTime;
import jfreerails.world.common.PositionOnTrack;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;
import jfreerails.world.train.ImmutableSchedule;
import jfreerails.world.train.MutableSchedule;
import jfreerails.world.train.Schedule;
import jfreerails.world.train.TrainModel;
import jfreerails.world.train.TrainOrdersModel;
import jfreerails.world.train.WagonType;


/**
 * This class provides methods that generate a path to a target as a series of
 * PositionOnTrack objects encoded as ints, it also deals with stops at
 * stations.
 *
 * @author Luke Lindsay 28-Nov-2002
 */
public class TrainPathFinder implements FreerailsIntIterator, ServerAutomaton {
    private static final Logger logger = Logger.getLogger(TrainPathFinder.class.getName());
    private static final int NOT_AT_STATION = -1;
    private final int trainId;
    private final ReadOnlyWorld world;
    private transient MoveReceiver moveReceiver;
    private final FlatTrackExplorer trackExplorer;
    private final SimpleAStarPathFinder pathFinder = new SimpleAStarPathFinder();
    private final FreerailsPrincipal principal;
    private GameTime timeLoadingFinished = new GameTime(0);
    private boolean waiting4FullLoad = false;
    private FreerailsSerializable lastCargoBundleAtStation = null;

    /**
     * Constructor.
     *
     * @param tx
     *            the track explorer this pathfinder is to use.
     */
    public TrainPathFinder(FlatTrackExplorer tx, ReadOnlyWorld w,
        int trainNumber, MoveReceiver mr, FreerailsPrincipal p) {
        this.moveReceiver = mr;
        this.trackExplorer = tx;
        this.trainId = trainNumber;
        this.world = w;
        principal = p;
    }

    public boolean hasNextInt() {
        if (isTrainMoving()) {
            return trackExplorer.hasNextEdge();
        } else {
            return false;
        }
    }

    /**
     * @return a move that initialises the trains schedule.
     */
    public static Move initTarget(TrainModel train, int trainID,
        ImmutableSchedule currentSchedule, FreerailsPrincipal principal) {
        Vector moves = new Vector();
        int scheduleID = train.getScheduleID();
        MutableSchedule schedule = new MutableSchedule(currentSchedule);
        int[] wagonsToAdd = schedule.getWagonsToAdd();

        if (null != wagonsToAdd) {
            int engine = train.getEngineType();
            ChangeTrainMove move = ChangeTrainMove.generateMove(trainID, train,
                    engine, wagonsToAdd, principal);
            moves.add(move);
        }

        schedule.gotoNextStaton();

        ImmutableSchedule newSchedule = schedule.toImmutableSchedule();
        ChangeTrainScheduleMove move = new ChangeTrainScheduleMove(scheduleID,
                currentSchedule, newSchedule, principal);
        moves.add(move);

        return new CompositeMove((Move[])moves.toArray(new Move[1]));
    }

    /**
     * Issues a ChangeTrainScheduleMove to set the train to move to the next
     * station.
     */
    private void updateTarget() {
        scheduledStop();
        updateSchedule();
    }

    private void updateSchedule() {
        TrainModel train = (TrainModel)world.get(KEY.TRAINS, this.trainId,
                principal);
        int scheduleID = train.getScheduleID();
        ImmutableSchedule currentSchedule = (ImmutableSchedule)world.get(KEY.TRAIN_SCHEDULES,
                scheduleID, principal);
        MutableSchedule schedule = new MutableSchedule(currentSchedule);
        StationModel station = null;

        TrainOrdersModel order = schedule.getOrder(schedule.getOrderToGoto());
        waiting4FullLoad = order.waitUntilFull && !isTrainFull();

        if (!waiting4FullLoad) {
            schedule.gotoNextStaton();

            ImmutableSchedule newSchedule = schedule.toImmutableSchedule();
            ChangeTrainScheduleMove move = new ChangeTrainScheduleMove(scheduleID,
                    currentSchedule, newSchedule, principal);
            moveReceiver.processMove(move);

            int stationNumber = schedule.getStationToGoto();
            station = (StationModel)world.get(KEY.STATIONS, stationNumber,
                    principal);

            if (null == station) {
                logger.warning("null == station, train " + trainId +
                    " doesn't know where to go next!");
            }
        }
    }

    /**
     * @return the location of the station the train is currently heading
     *         towards.
     */
    private Point getTarget() {
        TrainModel train = (TrainModel)world.get(KEY.TRAINS, this.trainId,
                principal);
        int scheduleID = train.getScheduleID();
        ImmutableSchedule schedule = (ImmutableSchedule)world.get(KEY.TRAIN_SCHEDULES,
                scheduleID, principal);
        int stationNumber = schedule.getStationToGoto();

        if (-1 == stationNumber) {
            //There are no stations on the schedule.
            return new Point(0, 0);
        }

        StationModel station = (StationModel)world.get(KEY.STATIONS,
                stationNumber, principal);

        return new Point(station.x, station.y);
    }

    private void scheduledStop() {
        TrainModel train = (TrainModel)world.get(KEY.TRAINS, this.trainId,
                principal);
        Schedule schedule = (ImmutableSchedule)world.get(KEY.TRAIN_SCHEDULES,
                train.getScheduleID(), principal);
        int[] wagonsToAdd = schedule.getWagonsToAdd();

        //Loading and unloading cargo takes time, so we make the train wait for
        // a few ticks.
        makeTrainWait(50);

        if (null != wagonsToAdd) {
            int engine = train.getEngineType();
            Move m = ChangeTrainMove.generateMove(this.trainId, train, engine,
                    wagonsToAdd, principal);
            moveReceiver.processMove(m);
        }
    }

    private void makeTrainWait(int ticks) {
        GameTime currentTime = (GameTime)world.get(ITEM.TIME);
        timeLoadingFinished = new GameTime(currentTime.getTime() + ticks);
    }

    private void loadAndUnloadCargo(int stationId, boolean waiting,
        boolean autoConsist) {
        /* We only want to generate a move if the station's cargo bundle is
         * not the last one we looked at.
         */
        StationModel station = (StationModel)world.get(KEY.STATIONS, stationId,
                principal);
        int cargoBundleId = station.getCargoBundleNumber();
        FreerailsSerializable currentCargoBundleAtStation = world.get(KEY.CARGO_BUNDLES,
                cargoBundleId, principal);

        if (currentCargoBundleAtStation != this.lastCargoBundleAtStation) {
            //train is at a station so do the cargo processing
            DropOffAndPickupCargoMoveGenerator transfer = new DropOffAndPickupCargoMoveGenerator(trainId,
                    stationId, world, principal, waiting, autoConsist);
            Move m = transfer.generateMove();
            moveReceiver.processMove(m);
            this.lastCargoBundleAtStation = currentCargoBundleAtStation;
        }
    }

    /**
     * @return the number of the station the train is currently at, or -1 if no
     *         current station.
     */
    private int getStationNumber(int x, int y) {
        //loop thru the station list to check if train is at the same Point as
        // a station
        for (int i = 0; i < world.size(KEY.STATIONS, principal); i++) {
            StationModel tempPoint = (StationModel)world.get(KEY.STATIONS, i,
                    principal);

            if (null != tempPoint && (x == tempPoint.x) && (y == tempPoint.y)) {
                return i; //train is at the station at location tempPoint
            }
        }

        return -1;
        //there are no stations that exist where the train is currently
    }

    /**
     * @return a PositionOnTrack packed into an int
     */
    public int nextInt() {
        PositionOnTrack tempP = new PositionOnTrack(trackExplorer.getPosition());
        Point targetPoint = getTarget();

        boolean autoConsist = false;

        if (tempP.getX() == targetPoint.x && tempP.getY() == targetPoint.y) {
            //One of the things updateTarget() does is change the train
            // consist, so
            //it should be called before loadAndUnloadCargo(stationNumber)
            TrainModel train = (TrainModel)world.get(KEY.TRAINS, this.trainId,
                    principal);
            Schedule schedule = (ImmutableSchedule)world.get(KEY.TRAIN_SCHEDULES,
                    train.getScheduleID(), principal);
            TrainOrdersModel order = schedule.getOrder(schedule.getOrderToGoto());

            autoConsist = order.autoConsist;

            updateTarget();
            targetPoint = getTarget();
        }

        int stationNumber = getStationNumber(tempP.getX(), tempP.getY());

        if (NOT_AT_STATION != stationNumber) {
            loadAndUnloadCargo(stationNumber, false, autoConsist);
        }

        int currentPosition = tempP.getOpposite().toInt();
        PositionOnTrack[] t = FlatTrackExplorer.getPossiblePositions(trackExplorer.getWorld(),
                new Point(targetPoint.x, targetPoint.y));
        int[] targets = new int[t.length];

        for (int i = 0; i < t.length; i++) {
            int target = t[i].getOpposite().toInt();

            if (target == currentPosition) {
                updateTarget();
            }

            targets[i] = target;
        }

        FlatTrackExplorer tempExplorer = new FlatTrackExplorer(trackExplorer.getWorld(),
                tempP);
        int next = pathFinder.findstep(currentPosition, targets, tempExplorer);

        if (next == SimpleAStarPathFinder.PATH_NOT_FOUND) {
            trackExplorer.nextEdge();
            trackExplorer.moveForward();

            return trackExplorer.getVertexConnectedByEdge();
        } else {
            tempP.setValuesFromInt(next);
            tempP = tempP.getOpposite();

            int nextPosition = tempP.toInt();
            trackExplorer.setPosition(nextPosition);

            return nextPosition;
        }
    }

    public void initAutomaton(MoveReceiver mr) {
        moveReceiver = mr;
    }

    private boolean isTrainMoving() {
        if (isWaitingForFullLoad()) {
            return false;
        } else {
            GameTime time = (GameTime)world.get(ITEM.TIME);

            return time.getTime() > this.timeLoadingFinished.getTime();
        }
    }

    private boolean isWaitingForFullLoad() {
        if (!waiting4FullLoad) {
            return false;
        } else {
            /* Check to see if the orders have changed */
            TrainModel train = (TrainModel)world.get(KEY.TRAINS, this.trainId,
                    principal);
            Schedule schedule = (ImmutableSchedule)world.get(KEY.TRAIN_SCHEDULES,
                    train.getScheduleID(), principal);
            TrainOrdersModel order = schedule.getOrder(schedule.getOrderToGoto());

            if (!order.waitUntilFull) {
                updateSchedule();

                return false;
            } else {
                /*Add any cargo that is waiting.*/
                loadAndUnloadCargo(schedule.getStationToGoto(), true, false);

                if (isTrainFull()) {
                    updateSchedule();

                    return false;
                }

                return true;
            }
        }
    }

    private boolean isTrainFull() {
        TrainModel train = (TrainModel)world.get(KEY.TRAINS, this.trainId,
                principal);
        ImmutableCargoBundle bundleOnTrain = (ImmutableCargoBundle)world.get(KEY.CARGO_BUNDLES,
                train.getCargoBundleNumber(), principal);

        //This array will store the amount of space available on the train for each cargo type.
        final int NUM_CARGO_TYPES = world.size(SKEY.CARGO_TYPES);
        int[] spaceAvailable = new int[NUM_CARGO_TYPES];

        //First calculate the train's total capacity.
        for (int j = 0; j < train.getNumberOfWagons(); j++) {
            int cargoType = train.getWagon(j);
            spaceAvailable[cargoType] += WagonType.UNITS_OF_CARGO_PER_WAGON;
        }

        for (int cargoType = 0; cargoType < NUM_CARGO_TYPES; cargoType++) {
            if (bundleOnTrain.getAmount(cargoType) < spaceAvailable[cargoType]) {
                return false;
            }
        }

        return true;
    }
}