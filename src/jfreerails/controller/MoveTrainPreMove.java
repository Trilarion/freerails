/*
 * Created on 18-Feb-2005
 *
 */
package jfreerails.controller;

import jfreerails.move.Move;
import jfreerails.move.NextActivityMove;
import jfreerails.world.common.ImPoint;
import jfreerails.world.common.PositionOnTrack;
import jfreerails.world.common.Step;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.AKEY;
import jfreerails.world.top.ActivityIterator;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.train.CompositeSpeedAgainstTime;
import jfreerails.world.train.ConstAcc;
import jfreerails.world.train.PathOnTiles;
import jfreerails.world.train.SpeedAgainstTime;
import jfreerails.world.train.TrainMotion;

/**
 * Generates moves for changes in train position and stops at stations.
 * 
 * @author Luke
 * 
 */
public class MoveTrainPreMove implements PreMove {
	private static final long serialVersionUID = 3545516188269491250L;

	private final FreerailsPrincipal principal;

	private final int trainID;

	public MoveTrainPreMove(int id, FreerailsPrincipal p) {
		trainID = id;
		principal = p;
	}

	double acceleration(int wagons) {
		return 1;
	}

	/**
	 * Returns true if
	 * <ol type="i">
	 * <li>the train is moving and a new train position is due.</li>
	 * <li>the train is waiting for a full load and there is more cargo to add.</li>
	 * <li>the train is stopped but due to start moving.</li>
	 * </ol>
	 */
	public boolean canGenerateMove(ReadOnlyWorld w) {
		return true;
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

		// Find the next vector.
		Step nextVector = nextVector(w);

		// Create a new train motion object.
		TrainMotion nextMotion = nextMotion(w, nextVector);

		// Create a new Move object.
		Move move = new NextActivityMove(nextMotion, trainID,
				AKEY.TRAIN_POSITIONS, principal);

		return move;
	}

	public int hashCode() {
		int result;
		result = trainID;
		result = 29 * result + principal.hashCode();
		return result;
	}

	private PositionOnTrack lastPosition(ReadOnlyWorld w) {
		ActivityIterator ai = w.getActivities(AKEY.TRAIN_POSITIONS, trainID,
				principal);
		while (ai.hasNext())
			ai.nextActivity();

		TrainMotion lastMotion = (TrainMotion) ai.getActivity();
		PositionOnTrack currentPosition = lastMotion.getFinalPosition();
		return currentPosition;
	}

	TrainMotion nextMotion(ReadOnlyWorld w, Step v) {
		ActivityIterator ai = w.getActivities(AKEY.TRAIN_POSITIONS, trainID,
				principal);
		while (ai.hasNext())
			ai.nextActivity();

		TrainMotion motion = (TrainMotion) ai.getActivity();

		SpeedAgainstTime speeds = nextSpeeds(w, v);

		PathOnTiles currentTiles = motion.getTiles(motion.duration());
		PathOnTiles pathOnTiles = currentTiles.addSteps(v);
		return new TrainMotion(pathOnTiles, currentTiles.steps(), motion
				.getTrainLength(), speeds);
	}

	SpeedAgainstTime nextSpeeds(ReadOnlyWorld w, Step v) {
		TrainAccessor ta = new TrainAccessor(w, principal, trainID);
		ActivityIterator ai = w.getActivities(AKEY.TRAIN_POSITIONS, trainID,
				principal);

		while (ai.hasNext())
			ai.nextActivity();

		TrainMotion lastMotion = (TrainMotion) ai.getActivity();

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

	Step nextVector(ReadOnlyWorld w) {
		// Find current position.
		PositionOnTrack currentPosition = lastPosition(w);
		// Find targets
		ImPoint targetPoint = currentTrainTarget(w);
		return findNextVector(w, currentPosition, targetPoint);
	}

	static Step findNextVector(ReadOnlyWorld w,
			PositionOnTrack currentPosition, ImPoint targetPoint) {
		PositionOnTrack[] t = FlatTrackExplorer.getPossiblePositions(w,
				targetPoint);
		int[] targets = new int[t.length];

		for (int i = 0; i < t.length; i++) {
			int target = t[i].getOpposite().toInt();
			targets[i] = target;
		}

		// Use path finder to decide where to go.

		FlatTrackExplorer tempExplorer = new FlatTrackExplorer(w,
				currentPosition);
		SimpleAStarPathFinder pathFinder = new SimpleAStarPathFinder();
		int next = pathFinder.findstep(currentPosition.toInt(), targets,
				tempExplorer);

		if (next == IncrementalPathFinder.PATH_NOT_FOUND) {
			// The pathfinder couldn't find a path so we
			// go in any legal direction.
			FlatTrackExplorer explorer = new FlatTrackExplorer(w,
					currentPosition);
			explorer.nextEdge();
			next = explorer.getVertexConnectedByEdge();
			// PositionOnTrack nextPosition = new PositionOnTrack(next);
			// return nextPosition.facing();
		}

		PositionOnTrack nextPosition = new PositionOnTrack(next);
		// XXX should really be nextPosition.facing(), but that produces the
		// wrong result!
		// I.e. the code somewhere else has facing and camefrom the wrong way
		// round.
		return nextPosition.cameFrom();
	}

	double topSpeed(int wagons) {
		return 100 / (wagons + 1);
	}
}
