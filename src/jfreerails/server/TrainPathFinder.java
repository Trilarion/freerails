package jfreerails.server;

import java.awt.Point;

import jfreerails.controller.MoveExecuter;
import jfreerails.controller.pathfinder.FlatTrackExplorer;
import jfreerails.controller.pathfinder.SimpleAStarPathFinder;
import jfreerails.move.ChangeTrainMove;
import jfreerails.move.ChangeTrainScheduleMove;
import jfreerails.move.Move;
import jfreerails.util.FreerailsIntIterator;
import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.PositionOnTrack;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.World;
import jfreerails.world.train.ImmutableSchedule;
import jfreerails.world.train.MutableSchedule;
import jfreerails.world.train.Schedule;
import jfreerails.world.train.TrainModel;

/**
 *
 * 28-Nov-2002
 * @author Luke Lindsay
 */
public class TrainPathFinder
	implements FreerailsIntIterator, FreerailsSerializable {

	public static final int NOT_AT_STATION = -1;

	private final int trainId;

	private final World world;

	FlatTrackExplorer trackExplorer;

	SimpleAStarPathFinder pathFinder = new SimpleAStarPathFinder();

	PositionOnTrack p1 = new PositionOnTrack();
	PositionOnTrack p2 = new PositionOnTrack();

	static final int TILE_SIZE = 30;

	public TrainPathFinder(FlatTrackExplorer tx, World w, int trainNumber) {
		this.trackExplorer = tx;
		this.trainId = trainNumber;
		this.world = w;

		updateTarget();
	}

	public boolean hasNextInt() {
		return trackExplorer.hasNextEdge();
	}

	/** updates the targetX and targetY values based on the train's schedule */
	private void updateTarget() {
		TrainModel train = (TrainModel) world.get(KEY.TRAINS, this.trainId);		
		int scheduleID = train.getScheduleID();
		ImmutableSchedule currentSchedule = (ImmutableSchedule)world.get(KEY.TRAIN_SCHEDULES, scheduleID);			
		MutableSchedule schedule = new MutableSchedule(currentSchedule);
		StationModel station = null;
		scheduledStop();					
		schedule.gotoNextStaton();
		ImmutableSchedule newSchedule = schedule.toImmutableSchedule();
		
		ChangeTrainScheduleMove move= new ChangeTrainScheduleMove(scheduleID,currentSchedule, newSchedule);
		MoveExecuter.getMoveExecuter().processMove(move);
		
		int stationNumber = schedule.getStationToGoto();
		station = (StationModel) world.get(KEY.STATIONS, stationNumber);
		if (null == station) {
			System.out.println(
				"null == station, train "
					+ trainId
					+ " doesn't know where to go next!");
		} else {
			//this.targetX = station.x;
			//this.targetY = station.y;
		}
	}
	

	private Point getTarget() {
		TrainModel train = (TrainModel) world.get(KEY.TRAINS, this.trainId);
		Schedule schedule = (ImmutableSchedule)world.get(KEY.TRAIN_SCHEDULES, train.getScheduleID());
		int stationNumber = schedule.getStationToGoto();
		StationModel station =
			(StationModel) world.get(KEY.STATIONS, stationNumber);
		return new Point(station.x, station.y);
	}

	private void scheduledStop() {
		TrainModel train = (TrainModel) world.get(KEY.TRAINS, this.trainId);
		Schedule schedule = (ImmutableSchedule)world.get(KEY.TRAIN_SCHEDULES, train.getScheduleID());
		StationModel station = null;
		int stationNumber = schedule.getStationToGoto();
		station = (StationModel) world.get(KEY.STATIONS, stationNumber);
		int[] wagonsToAdd = schedule.getWagonsToAdd();
		if (null != wagonsToAdd) {
			int engine = train.getEngineType();
			Move m = ChangeTrainMove.generateMove(this.trainId, train, engine, wagonsToAdd);
			MoveExecuter.getMoveExecuter().processMove(m);
		}
	}

	private void loadAndUnloadCargo(int stationId) {
		System.out.println("Train " + trainId + " is at station " + stationId);
		//train is at a station so do the cargo processing

		DropOffAndPickupCargoMoveGenerator transfer =
			new DropOffAndPickupCargoMoveGenerator(trainId, stationId, world);

		Move m = transfer.generateMove();
		m.doMove(this.world);
	}

	public int getStationNumber(int x, int y) {

		//loop thru the station list to check if train is at the same Point as a station
		for (int i = 0; i < world.size(KEY.STATIONS); i++) {
			StationModel tempPoint = (StationModel) world.get(KEY.STATIONS, i);
			if (null != tempPoint
				&& (x == tempPoint.x)
				&& (y == tempPoint.y)) {
				return i; //train is at the station at location tempPoint
			}
		}
		return -1;
		//there are no stations that exist where the train is currently
	}

	public int nextInt() {

		PositionOnTrack tempP =
			new PositionOnTrack(trackExplorer.getPosition());
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

		PositionOnTrack[] t =
			FlatTrackExplorer.getPossiblePositions(
				trackExplorer.getWorld(),
				new Point(targetPoint.x, targetPoint.y));
		int[] targets = new int[t.length];
		for (int i = 0; i < t.length; i++) {
			int target = t[i].getOpposite().toInt();
			if (target == currentPosition) {
				System.out.println("Reached target!");
				updateTarget();
			}
			targets[i] = target;
		}

		FlatTrackExplorer tempExplorer =
			new FlatTrackExplorer(trackExplorer.getWorld(), tempP);
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

}
