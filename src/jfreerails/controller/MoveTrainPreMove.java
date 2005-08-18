/*
 * Created on 18-Feb-2005
 *
 */
package jfreerails.controller;

import static jfreerails.world.train.SpeedTimeAndStatus.Activity.STOPPED_AT_STATION;
import static jfreerails.world.train.SpeedTimeAndStatus.Activity.WAITING_FOR_FULL_LOAD;

import java.util.ArrayList;
import java.util.List;

import jfreerails.move.CompositeMove;
import jfreerails.move.Move;
import jfreerails.move.NextActivityMove;
import jfreerails.world.common.ActivityIterator;
import jfreerails.world.common.GameTime;
import jfreerails.world.common.ImPoint;
import jfreerails.world.common.PositionOnTrack;
import jfreerails.world.common.Step;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.WorldDiffs;
import jfreerails.world.train.CompositeSpeedAgainstTime;
import jfreerails.world.train.ConstAcc;
import jfreerails.world.train.PathOnTiles;
import jfreerails.world.train.SpeedAgainstTime;
import jfreerails.world.train.SpeedTimeAndStatus;
import jfreerails.world.train.TrainMotion;

/**
 * Generates moves for changes in train position and stops at stations.
 * 
 * @author Luke
 * 
 */
public class MoveTrainPreMove implements PreMove {
	private static final long serialVersionUID = 3545516188269491250L;

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
	 * Returns true iff if an updated is due.
	 * 
	 */
	public boolean canGenerateMove(ReadOnlyWorld w) {
		GameTime currentTime = w.currentTime();
		ActivityIterator ai = w.getActivities(principal, trainID);
		while (ai.hasNext())
			ai.nextActivity();
				
		double finishTime = ai.getFinishTime();
		double ticks = currentTime.getTicks();
		Math.floor(finishTime);

		return Math.floor(finishTime) <= ticks;
	}

	private TrainMotion currentMotion(ReadOnlyWorld w) {
		ActivityIterator ai = w.getActivities(principal, trainID);
		while (ai.hasNext())
			ai.nextActivity();

		TrainMotion lastMotion = (TrainMotion) ai.getActivity();
		return lastMotion;
	}

	private ImPoint currentTrainTarget(ReadOnlyWorld w) {
		TrainAccessor ta = new TrainAccessor(w, principal, trainID);
		return ta.getTarget();
	}

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
		if (!canGenerateMove(w))
			throw new IllegalStateException();

		TrainAccessor ta = new TrainAccessor(w, principal, trainID);
		TrainMotion tm = ta.findCurrentMotion(Integer.MAX_VALUE);

		SpeedTimeAndStatus.Activity activity = tm.getActivity();

		switch (activity) {
		case STOPPED_AT_STATION:
			return moveTrain(w);
		case READY:
		{
			// Are we at a station?
			TrainStopsHandler stopsHandler = new TrainStopsHandler(trainID,
					principal, new WorldDiffs(w));
			PositionOnTrack pot = tm.getFinalPosition();
			int x = pot.getX();
			int y = pot.getY();
			boolean atStation = stopsHandler.getStationID(x, y) >= 0;

			TrainMotion nextMotion;
			if (atStation) {
				// We have just arrived at a station.
				double durationOfStationStop = 10;
								
				stopsHandler.arrivesAtPoint(x, y);
				SpeedTimeAndStatus.Activity status = stopsHandler.isWaiting4FullLoad() ? WAITING_FOR_FULL_LOAD : STOPPED_AT_STATION;
				PathOnTiles path = tm.getPath();
				int lastTrainLength = tm.getTrainLength();
				int currentTrainLength = stopsHandler.getTrainLength();
				
				//If we are adding wagons we may need to lengthen the path.
				if(lastTrainLength < currentTrainLength){
					double pathDistance = path.getTotalDistance();
					double extraDistanceNeeded = currentTrainLength - pathDistance;
					
					List<Step> steps = new ArrayList<Step>();
					ImPoint start = path.getStart();
					Step firstStep = path.getStep(0);
					PositionOnTrack nextPot = PositionOnTrack.createComingFrom(start.x, start.y, firstStep);
					
					while( extraDistanceNeeded > 0){
						
						FlatTrackExplorer fte = new FlatTrackExplorer(w, nextPot);
						fte.nextEdge();
						nextPot.setValuesFromInt(fte.getVertexConnectedByEdge());
						Step cameFrom = nextPot.facing();
						steps.add(0, cameFrom);
						extraDistanceNeeded -= cameFrom.getLength();
						
					}
					
					//Add existing steps
					for (int i = 0; i < path.steps(); i++) {
						Step step = path.getStep(i);	
						steps.add(step);
					}
					
					ImPoint newStart = new ImPoint(nextPot.getX(), nextPot.getY());
					path = new PathOnTiles(newStart, steps);
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
			
			//If we don't call setWaiting4FullLoad(true), then refreshWaitingForFullLoad();
			//will return false straight away.
			stopsHandler.setWaiting4FullLoad(true);
			boolean waiting4fullLoad = stopsHandler.refreshWaitingForFullLoad();
			Move cargoMove = stopsHandler.getMoves();
			if(!waiting4fullLoad){
				Move trainMove = moveTrain(w);
				return new CompositeMove(trainMove, cargoMove);
			}
			return cargoMove;
			
		}
		default:
			throw new UnsupportedOperationException(activity.toString());
		}

		// then stop train and unload cargo.
		// Are we waiting?
		// Have orders changed?
		// Yes, then updated schedule.

		// Load/Unload any cargo.
		// Decide whether to start moving.

	}

	// boolean isTrainAtStation(){
	//		
	// }

	public int hashCode() {
		int result;
		result = trainID;
		result = 29 * result + principal.hashCode();
		return result;
	}

	private Move moveTrain(ReadOnlyWorld w) {
		// Find the next vector.
		Step nextVector = nextStep(w);

		// Create a new train motion object.
		TrainMotion nextMotion = nextMotion(w, nextVector);

		return new NextActivityMove(nextMotion, trainID, principal);

	}

	TrainMotion nextMotion(ReadOnlyWorld w, Step v) {
		TrainMotion motion = currentMotion(w);

		SpeedAgainstTime speeds = nextSpeeds(w, v);

		PathOnTiles currentTiles = motion.getTiles(motion.duration());
		PathOnTiles pathOnTiles = currentTiles.addSteps(v);
		return new TrainMotion(pathOnTiles, currentTiles.steps(), motion
				.getTrainLength(), speeds);
	}

	SpeedAgainstTime nextSpeeds(ReadOnlyWorld w, Step v) {
		TrainAccessor ta = new TrainAccessor(w, principal, trainID);
		TrainMotion lastMotion = currentMotion(w);

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
		TrainMotion currentMotion = currentMotion(w);
		PositionOnTrack currentPosition = currentMotion.getFinalPosition();
		// Find targets
		ImPoint targetPoint = currentTrainTarget(w);
		return findNextStep(w, currentPosition, targetPoint);
	}	
	
	public Move stopTrain(ReadOnlyWorld w) {
		TrainMotion motion = currentMotion(w);
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
