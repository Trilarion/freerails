package jfreerails.server;

import java.awt.Point;
import java.util.Vector;
import jfreerails.controller.MoveReceiver;
import jfreerails.controller.pathfinder.FlatTrackExplorer;
import jfreerails.controller.pathfinder.SimpleAStarPathFinder;
import jfreerails.move.ChangeTrainMove;
import jfreerails.move.ChangeTrainScheduleMove;
import jfreerails.move.CompositeMove;
import jfreerails.move.Move;
import jfreerails.util.FreerailsIntIterator;
import jfreerails.server.ServerAutomaton;
import jfreerails.world.common.PositionOnTrack;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.train.ImmutableSchedule;
import jfreerails.world.train.MutableSchedule;
import jfreerails.world.train.Schedule;
import jfreerails.world.train.TrainModel;


/**
 * This class provides methods that generate a path to a target as a series of
 * PositionOnTrack objects encoded as ints.
 *
 *
 * @author Luke Lindsay
 * 28-Nov-2002
 */
public class TrainPathFinder implements FreerailsIntIterator, ServerAutomaton {
    public static final int NOT_AT_STATION = -1;
    private final int trainId;
    private final ReadOnlyWorld world;
    private transient MoveReceiver moveReceiver;
    FlatTrackExplorer trackExplorer;
    SimpleAStarPathFinder pathFinder = new SimpleAStarPathFinder();
    PositionOnTrack p1 = new PositionOnTrack();
    PositionOnTrack p2 = new PositionOnTrack();
    static final int TILE_SIZE = 30;

    /**
     * Constructor.
     *
     * @param tx the track explorer this pathfinder is to use.
     */
    public TrainPathFinder(FlatTrackExplorer tx, ReadOnlyWorld w,
        int trainNumber, MoveReceiver mr) {
        this.moveReceiver = mr;
        this.trackExplorer = tx;
        this.trainId = trainNumber;
        this.world = w;
    }

    public boolean hasNextInt() {
        return trackExplorer.hasNextEdge();
    }

    /**
     * @return a move that initialises the trains schedule.
     */
    public Move initTarget(TrainModel train, ImmutableSchedule currentSchedule) {
        Vector moves = new Vector();
        int scheduleID = train.getScheduleID();
        MutableSchedule schedule = new MutableSchedule(currentSchedule);
        StationModel station = null;
        int stationNumber = schedule.getStationToGoto();
        station = (StationModel)world.get(KEY.STATIONS, stationNumber);

        int[] wagonsToAdd = schedule.getWagonsToAdd();

        if (null != wagonsToAdd) {
            int engine = train.getEngineType();
            moves.add(ChangeTrainMove.generateMove(this.trainId, train, engine,
                    wagonsToAdd));
        }

        schedule.gotoNextStaton();

        ImmutableSchedule newSchedule = schedule.toImmutableSchedule();

        ChangeTrainScheduleMove move = new ChangeTrainScheduleMove(scheduleID,
                currentSchedule, newSchedule);
        moves.add(move);

        return new CompositeMove((Move[])moves.toArray(new Move[1]));
    }

    /**
     * Issues a ChangeTrainScheduleMove to set the train to move to the next
     * station.
     */
    private void updateTarget() {
        TrainModel train = (TrainModel)world.get(KEY.TRAINS, this.trainId);
        int scheduleID = train.getScheduleID();
        ImmutableSchedule currentSchedule = (ImmutableSchedule)world.get(KEY.TRAIN_SCHEDULES,
                scheduleID);
        MutableSchedule schedule = new MutableSchedule(currentSchedule);
        StationModel station = null;
        scheduledStop();
        schedule.gotoNextStaton();

        ImmutableSchedule newSchedule = schedule.toImmutableSchedule();

        ChangeTrainScheduleMove move = new ChangeTrainScheduleMove(scheduleID,
                currentSchedule, newSchedule);
        moveReceiver.processMove(move);

        int stationNumber = schedule.getStationToGoto();
        station = (StationModel)world.get(KEY.STATIONS, stationNumber);

        if (null == station) {
            System.err.println("null == station, train " + trainId +
                " doesn't know where to go next!");
        } else {
            //this.targetX = station.x;
            //this.targetY = station.y;
        }
    }

    /**
     * @return the location of the station the train is currently heading
     * towards.
     */
    private Point getTarget() {
        TrainModel train = (TrainModel)world.get(KEY.TRAINS, this.trainId);
        int scheduleID = train.getScheduleID();
        ImmutableSchedule schedule = (ImmutableSchedule)world.get(KEY.TRAIN_SCHEDULES,
                scheduleID);
        int stationNumber = schedule.getStationToGoto();

        if (-1 == stationNumber) {
            //There are no stations on the schedule.
            return new Point(0, 0);
        }

        StationModel station = (StationModel)world.get(KEY.STATIONS,
                stationNumber);

        return new Point(station.x, station.y);
    }

    private void scheduledStop() {
        TrainModel train = (TrainModel)world.get(KEY.TRAINS, this.trainId);
        Schedule schedule = (ImmutableSchedule)world.get(KEY.TRAIN_SCHEDULES,
                train.getScheduleID());
        StationModel station = null;
        int stationNumber = schedule.getStationToGoto();
        station = (StationModel)world.get(KEY.STATIONS, stationNumber);

        int[] wagonsToAdd = schedule.getWagonsToAdd();

        if (null != wagonsToAdd) {
            int engine = train.getEngineType();
            Move m = ChangeTrainMove.generateMove(this.trainId, train, engine,
                    wagonsToAdd);
            moveReceiver.processMove(m);
        }
    }

    private void loadAndUnloadCargo(int stationId) {
        //train is at a station so do the cargo processing
        DropOffAndPickupCargoMoveGenerator transfer = new DropOffAndPickupCargoMoveGenerator(trainId,
                stationId, world);

        Move m = transfer.generateMove();
        moveReceiver.processMove(m);
    }

    /**
     * @return the number of the station the train is currently at, or -1 if
     * no current station.
     */
    public int getStationNumber(int x, int y) {
        //loop thru the station list to check if train is at the same Point as a station
        for (int i = 0; i < world.size(KEY.STATIONS); i++) {
            StationModel tempPoint = (StationModel)world.get(KEY.STATIONS, i);

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

        if (tempP.getX() == targetPoint.x && tempP.getY() == targetPoint.y) {
            //One of the things updateTarget() does is change the train consist, so
            //it should be called before loadAndUnloadCargo(stationNumber)
            updateTarget();
            targetPoint = getTarget();
        }

        int stationNumber = getStationNumber(tempP.getX(), tempP.getY());

        if (NOT_AT_STATION != stationNumber) {
            loadAndUnloadCargo(stationNumber);
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
        int next = pathFinder.findpath(currentPosition, targets, tempExplorer);

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
}