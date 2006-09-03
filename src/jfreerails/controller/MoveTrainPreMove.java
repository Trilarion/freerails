/*
 * Created on 18-Feb-2005
 *
 */
package jfreerails.controller;

import static jfreerails.world.train.SpeedTimeAndStatus.TrainActivity.STOPPED_AT_STATION;
import static jfreerails.world.train.SpeedTimeAndStatus.TrainActivity.WAITING_FOR_FULL_LOAD;

import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;

import jfreerails.move.CompositeMove;
import jfreerails.move.Move;
import jfreerails.move.NextActivityMove;
import jfreerails.world.cargo.CargoBundle;
import jfreerails.world.common.ActivityIterator;
import jfreerails.world.common.GameTime;
import jfreerails.world.common.ImInts;
import jfreerails.world.common.ImPoint;
import jfreerails.world.common.PositionOnTrack;
import jfreerails.world.common.Step;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.WorldDiffs;
import jfreerails.world.track.FreerailsTile;
import jfreerails.world.track.TrackPiece;
import jfreerails.world.track.TrackSection;
import jfreerails.world.train.CompositeSpeedAgainstTime;
import jfreerails.world.train.ConstAcc;
import jfreerails.world.train.PathOnTiles;
import jfreerails.world.train.SpeedAgainstTime;
import jfreerails.world.train.SpeedTimeAndStatus;
import jfreerails.world.train.TrainModel;
import jfreerails.world.train.TrainMotion;
import jfreerails.world.train.SpeedTimeAndStatus.TrainActivity;

/**
 * Generates moves for changes in train position and stops at stations.
 * 
 * @author Luke
 * 
 */
public class MoveTrainPreMove implements PreMove {
	private static final long serialVersionUID = 3545516188269491250L;
	private static final Logger logger = Logger.getLogger(MoveTrainPreMove.class
			.getName());

	/** Uses static method to make testing easier.*/
	public static Step findNextStep(ReadOnlyWorld world,
			PositionOnTrack currentPosition, ImPoint target) {
		PathOnTrackFinder pathFinder = new PathOnTrackFinder(world);

		try {
			ImPoint location = new ImPoint(currentPosition.getX(),
					currentPosition.getY());
			pathFinder.setupSearch(location, target);
			pathFinder.search(-1);
			return pathFinder.pathAsVectors()[0];
		} catch (PathNotFoundException e) {
			// The pathfinder couldn't find a path so we
			// go in any legal direction.
			FlatTrackExplorer explorer = new FlatTrackExplorer(world,
					currentPosition);
			explorer.nextEdge();
			int next = explorer.getVertexConnectedByEdge();
			PositionOnTrack nextPosition = new PositionOnTrack(next);
			return nextPosition.cameFrom();
		}

	}

	private final FreerailsPrincipal principal;

	private final int trainID;

	public MoveTrainPreMove(int id, FreerailsPrincipal p) {
		trainID = id;
		principal = p;
	}

	double acceleration(int wagons) {
		return 0.5d/(wagons + 1);
	}

	/**
	 * Returns true iff an updated is due.
	 * 
	 */
	public boolean isUpdateDue(ReadOnlyWorld w) {
		GameTime currentTime = w.currentTime();
		TrainAccessor ta = new TrainAccessor(w, principal, trainID);
		ActivityIterator ai = w.getActivities(principal, trainID);
		while (ai.hasNext())
			ai.nextActivity();

		double finishTime = ai.getFinishTime();
		double ticks = currentTime.getTicks();

		boolean hasFinishedLastActivity = Math.floor(finishTime) <= ticks;
		TrainActivity trainActivity = ta.getStatus(finishTime);
		if(trainActivity == TrainActivity.WAITING_FOR_FULL_LOAD){
			//Check whether there is any cargo that can be added to the train.
			ImInts spaceAvailable = ta.spaceAvailable();
			int stationId = ta.getStationId(ticks);
			if(stationId == -1)
				throw new IllegalStateException();
			
			StationModel station = (StationModel)w.get(principal, KEY.STATIONS, stationId);
			CargoBundle cb = (CargoBundle)w.get(principal, KEY.CARGO_BUNDLES, station.getCargoBundleID());
			
			for(int i = 0; i < spaceAvailable.size(); i++){
				int space = spaceAvailable.get(i);
				int atStation = cb.getAmount(i);
				if(space * atStation > 0){
					logger.fine("There is cargo to transfer!");
					return true;
				}
			}
			
			return !ta.keepWaiting();
		}
		return hasFinishedLastActivity;
	}		

	private ImPoint currentTrainTarget(ReadOnlyWorld w) {
		TrainAccessor ta = new TrainAccessor(w, principal, trainID);
		return ta.getTarget();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof MoveTrainPreMove))
			return false;

		final MoveTrainPreMove moveTrainPreMove = (MoveTrainPreMove) o;

		if (trainID != moveTrainPreMove.trainID)
			return false;
		if (!principal.equals(moveTrainPreMove.principal))
			return false;

		return true;
	}

	public Move generateMove(ReadOnlyWorld w) {

		// Check that we can generate a move.
		if (!isUpdateDue(w))
			throw new IllegalStateException();

		TrainAccessor ta = new TrainAccessor(w, principal, trainID);
		TrainMotion tm = ta.findCurrentMotion(Double.MAX_VALUE);

		SpeedTimeAndStatus.TrainActivity activity = tm.getActivity();

		switch (activity) {
		case STOPPED_AT_STATION:
			return moveTrain(w);
		case READY:
		{
			// Are we at a station?
			TrainStopsHandler stopsHandler = new TrainStopsHandler(trainID,
					principal, new WorldDiffs(w));
			ta.getStationId(Integer.MAX_VALUE);
			PositionOnTrack pot = tm.getFinalPosition();
			int x = pot.getX();
			int y = pot.getY();
			boolean atStation = stopsHandler.getStationID(x, y) >= 0;

			TrainMotion nextMotion;
			if (atStation) {
				// We have just arrived at a station.
				double durationOfStationStop = 10;
								
				stopsHandler.arrivesAtPoint(x, y);
				
				SpeedTimeAndStatus.TrainActivity status = stopsHandler.isWaiting4FullLoad() ? WAITING_FOR_FULL_LOAD : STOPPED_AT_STATION;
				PathOnTiles path = tm.getPath();
				int lastTrainLength = tm.getTrainLength();
				int currentTrainLength = stopsHandler.getTrainLength();
				
				//If we are adding wagons we may need to lengthen the path.
				if(lastTrainLength < currentTrainLength){
					path = TrainStopsHandler.lengthenPath(w, path, currentTrainLength);
				}
				
				nextMotion = new TrainMotion(path, currentTrainLength,
						durationOfStationStop, status);
				
				// Create a new Move object.
				Move trainMove = new NextActivityMove(nextMotion, trainID,
						principal);

				Move cargoMove = stopsHandler.getMoves();
				return new CompositeMove(trainMove, cargoMove);
			}
			return moveTrain(w);
		}	
		case WAITING_FOR_FULL_LOAD:
		{
			TrainStopsHandler stopsHandler = new TrainStopsHandler(trainID,
					principal, new WorldDiffs(w));
			
			
			boolean waiting4fullLoad = stopsHandler.refreshWaitingForFullLoad();
			Move cargoMove = stopsHandler.getMoves();
			if(!waiting4fullLoad){
				Move trainMove = moveTrain(w);
				if(null != trainMove){
				return new CompositeMove(trainMove, cargoMove);
				}else{
					return cargoMove;
				}
			}
			stopsHandler.makeTrainWait(30);
			return cargoMove;
			
		}
		default:
			throw new UnsupportedOperationException(activity.toString());
		}
	}
	
	public SpeedTimeAndStatus.TrainActivity getActivity(ReadOnlyWorld w){
		TrainAccessor ta = new TrainAccessor(w, principal, trainID);
		TrainMotion tm = ta.findCurrentMotion(Integer.MAX_VALUE);
		return tm.getActivity();		
	}

	@Override
	public int hashCode() {
		int result;
		result = trainID;
		result = 29 * result + principal.hashCode();
		return result;
	}

	private TrainMotion lastMotion(ReadOnlyWorld w) {
		ActivityIterator ai = w.getActivities(principal, trainID);
		while (ai.hasNext())
			ai.nextActivity();

		TrainMotion lastMotion = (TrainMotion) ai.getActivity();
		return lastMotion;
	}

	private Move moveTrain(ReadOnlyWorld w) {
		// Find the next vector.
		Step nextVector = nextStep(w);
		HashMap<TrackSection, Integer> occupiedTrackSections = occupiedTrackSections(w);
		TrainMotion motion = lastMotion(w);
		PositionOnTrack pot = motion.getFinalPosition();
		ImPoint tile = new ImPoint(pot.getX(), pot.getY());
		TrackSection desiredTrackSection = new TrackSection(nextVector, tile);

		// Check whether the desired track section is single or double track.
		ImPoint tileA = desiredTrackSection.tileA();
		ImPoint tileB = desiredTrackSection.tileB();
		FreerailsTile fta = (FreerailsTile) w.getTile(tileA.x, tileA.y);
		FreerailsTile ftb = (FreerailsTile) w.getTile(tileB.x, tileB.y);
		TrackPiece tpa = fta.getTrackPiece();
		TrackPiece tpb = ftb.getTrackPiece();
		int tracks = 1;
		if (tpa.getTrackRule().isDouble() && tpb.getTrackRule().isDouble()) {
			tracks = 2;
		}

		if (occupiedTrackSections.containsKey(desiredTrackSection)) {
			int trains = occupiedTrackSections.get(desiredTrackSection);
			if (trains >= tracks) {
				// We need to wait for the track ahead to clear.
				return stopTrain(w);
			}
		}
		// Create a new train motion object.
		TrainMotion nextMotion = nextMotion(w, nextVector);
		return new NextActivityMove(nextMotion, trainID, principal);

	}

	private HashMap<TrackSection, Integer> occupiedTrackSections(ReadOnlyWorld w) {
		HashMap<TrackSection, Integer> occupiedTrackSections = new HashMap<TrackSection, Integer>();
		for (int i = 0; i < w.size(principal, KEY.TRAINS); i++) {						
			TrainModel train = (TrainModel) w.get(principal,
					KEY.TRAINS, i);
			if (null == train)
				continue;
			
			TrainAccessor ta = new TrainAccessor(w, principal, i);
			GameTime gt = w.currentTime();
			if(ta.isMoving(gt.getTicks())){
				HashSet<TrackSection> sections = ta.occupiedTrackSection(gt.getTicks());
				for (TrackSection section : sections) {
					if(occupiedTrackSections.containsKey(section)){
						int count = occupiedTrackSections.get(section);
						count++;
						occupiedTrackSections.put(section, count);
					}else{
						occupiedTrackSections.put(section, 1);
					}
				}						
			}
		}
		return occupiedTrackSections;
	}

	TrainMotion nextMotion(ReadOnlyWorld w, Step v) {
		TrainMotion motion = lastMotion(w);

		SpeedAgainstTime speeds = nextSpeeds(w, v);

		PathOnTiles currentTiles = motion.getTiles(motion.duration());
		PathOnTiles pathOnTiles = currentTiles.addSteps(v);
		return new TrainMotion(pathOnTiles, currentTiles.steps(), motion
				.getTrainLength(), speeds);
	}

	SpeedAgainstTime nextSpeeds(ReadOnlyWorld w, Step v) {
		TrainAccessor ta = new TrainAccessor(w, principal, trainID);
		TrainMotion lastMotion = lastMotion(w);

		double u = lastMotion.getSpeedAtEnd();
		double s = v.getLength();

		int wagons = ta.getTrain().getNumberOfWagons();
		double a0 = acceleration(wagons);
		double topSpeed = topSpeed(wagons);

		SpeedAgainstTime newSpeeds;
		if (u < topSpeed) {
			double t = (topSpeed - u) / a0;
			SpeedAgainstTime a = ConstAcc.uat(u, a0, t);
			t = s / topSpeed + 1; // Slightly overestimate the time
			SpeedAgainstTime b = ConstAcc.uat(topSpeed, 0, t);
			newSpeeds = new CompositeSpeedAgainstTime(a, b);
		} else {
			double t;
			t = s / topSpeed + 1; // Slightly overestimate the time
			newSpeeds = ConstAcc.uat(topSpeed, 0, t);
		}

		return newSpeeds;
	}

	Step nextStep(ReadOnlyWorld w) {
		// Find current position.
		TrainMotion currentMotion = lastMotion(w);
		PositionOnTrack currentPosition = currentMotion.getFinalPosition();
		// Find targets
		ImPoint targetPoint = currentTrainTarget(w);
		return findNextStep(w, currentPosition, targetPoint);
	}	
	
	public Move stopTrain(ReadOnlyWorld w) {
		TrainMotion motion = lastMotion(w);
		SpeedAgainstTime stopped = ConstAcc.STOPPED;
		double duration = motion.duration();

		int trainLength = motion.getTrainLength();
		PathOnTiles tiles = motion.getTiles(duration);
		int engineDist = tiles.steps();
		TrainMotion nextMotion = new TrainMotion(tiles, engineDist,
				trainLength, stopped);
		return new NextActivityMove(nextMotion, trainID, principal);
	}

	double topSpeed(int wagons) {
		return 10 / (wagons + 1);
	}
}
