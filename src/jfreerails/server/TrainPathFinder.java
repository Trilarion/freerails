package jfreerails.server;

import java.awt.Point;

import jfreerails.controller.FlatTrackExplorer;
import jfreerails.controller.IncrementalPathFinder;
import jfreerails.controller.SimpleAStarPathFinder;
import jfreerails.network.MoveReceiver;
import jfreerails.util.FreerailsIntIterator;
import jfreerails.world.common.PositionOnTrack;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.train.ImmutableSchedule;
import jfreerails.world.train.Schedule;
import jfreerails.world.train.TrainModel;
import jfreerails.world.train.TrainOrdersModel;


/**
 * This class provides methods that generate a path to a target as a series of
 * PositionOnTrack objects encoded as ints, it also deals with stops at
 * stations.
 *
 * @author Luke Lindsay 28-Nov-2002
 */
public class TrainPathFinder implements FreerailsIntIterator, ServerAutomaton {
    private static final long serialVersionUID = 3256446893302559280L;
	private static final int NOT_AT_STATION = -1;
    private final SimpleAStarPathFinder pathFinder = new SimpleAStarPathFinder();
    private final FreerailsPrincipal principal;   
    private final FlatTrackExplorer trackExplorer;
    private final int trainId;
    private final ReadOnlyWorld world;
    private final TrainStopsHandler stopsHandler;

    
    public TrainPathFinder(FlatTrackExplorer tx, ReadOnlyWorld w,
        int trainNumber, MoveReceiver mr, FreerailsPrincipal p) {
        this.trackExplorer = tx;
        this.trainId = trainNumber;
        this.world = w;
        principal = p;
        stopsHandler = new TrainStopsHandler(trainId, principal, world, mr);        
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

    public boolean hasNextInt() {
        if (stopsHandler.isTrainMoving()) {
            return trackExplorer.hasNextEdge();
        }
		return false;
    }

    public void initAutomaton(MoveReceiver mr) {
    	stopsHandler.initAutomaton(mr);
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

            stopsHandler.updateTarget();
            targetPoint = getTarget();
        }

        int stationNumber = stopsHandler.getStationID(tempP.getX(), tempP.getY());

        if (NOT_AT_STATION != stationNumber) {
        	stopsHandler.loadAndUnloadCargo(stationNumber, false, autoConsist);
        }

        int currentPosition = tempP.getOpposite().toInt();
        PositionOnTrack[] t = FlatTrackExplorer.getPossiblePositions(trackExplorer.getWorld(),
                new Point(targetPoint.x, targetPoint.y));
        int[] targets = new int[t.length];

        for (int i = 0; i < t.length; i++) {
            int target = t[i].getOpposite().toInt();

            if (target == currentPosition) {
                stopsHandler.updateTarget();
            }

            targets[i] = target;
        }

        FlatTrackExplorer tempExplorer = new FlatTrackExplorer(trackExplorer.getWorld(),
                tempP);
        int next = pathFinder.findstep(currentPosition, targets, tempExplorer);

        if (next == IncrementalPathFinder.PATH_NOT_FOUND) {
            trackExplorer.nextEdge();
            trackExplorer.moveForward();

            return trackExplorer.getVertexConnectedByEdge();
        }
		tempP.setValuesFromInt(next);
		tempP = tempP.getOpposite();

		int nextPosition = tempP.toInt();
		trackExplorer.setPosition(nextPosition);

		return nextPosition;
    }

    
}