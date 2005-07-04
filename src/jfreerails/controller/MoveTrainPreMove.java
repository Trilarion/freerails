/*
 * Created on 18-Feb-2005
 *
 */
package jfreerails.controller;

import java.awt.Point;

import jfreerails.move.ChangeItemInListMove;
import jfreerails.move.Move;
import jfreerails.world.common.GameTime;
import jfreerails.world.common.Step;
import jfreerails.world.common.PositionOnTrack;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.train.SpeedAgainstTime;
import jfreerails.world.train.TrainMotion;
import jfreerails.world.train.SpeedTimeAndStatus.Activity;

/**
 * Generates moves for changes in train position and stops at stations.
 * 
 * @author Luke
 * 
 */
public class MoveTrainPreMove implements PreMove {
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

	public int hashCode() {
		int result;
		result = trainID;
		result = 29 * result + principal.hashCode();
		return result;
	}

	private static final long serialVersionUID = 3545516188269491250L;

	private final int trainID;

	private final FreerailsPrincipal principal;

	public MoveTrainPreMove(int id, FreerailsPrincipal p) {
		trainID = id;
		principal = p;
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
		TrainAccessor ta = new TrainAccessor(w, principal, trainID);
		GameTime currentTime = w.currentTime();
		Activity a = ta.getActivity(currentTime);
		return a.equals(Activity.NEEDS_UPDATING);
	}

	public Move generateMove(ReadOnlyWorld w) {

		// Check that we can generate a move.
		if (!canGenerateMove(w))
			throw new IllegalStateException();

		// Find the next vector.
		Step nextVector = nextVector(w);
		GameTime currentTime = w.currentTime();

		// Create a new train motion object.
		TrainMotion nextMotion = nextMotion(w, nextVector, currentTime);

		// Create a new Move object.
		TrainAccessor ta = new TrainAccessor(w, principal, trainID);
		KEY k = ta.getFirstKEY();
		TrainMotion oldMotion = (TrainMotion) w.get(k, trainID, principal);

		ChangeItemInListMove move = new ChangeItemInListMove(k, trainID,
				oldMotion, nextMotion, principal);

		return move;
	}

	Step nextVector(ReadOnlyWorld w) {
		// Find current position.
		PositionOnTrack currentPosition = currentTrainPosition(w);
		// Find targets
		Point targetPoint = currentTrainTarget(w);
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
		PositionOnTrack nextPosition = new PositionOnTrack(next);
		// XXX should really be nextPosition.facing(), but that produces the
		// wrong result!
		// I.e. the code somewhere else has facing and camefrom the wrong way
		// round.
		return nextPosition.cameFrom();
	}

	private PositionOnTrack currentTrainPosition(ReadOnlyWorld w) {
		TrainAccessor ta = new TrainAccessor(w, principal, trainID);
		KEY k = ta.getLastKEY();
		TrainMotion lastMotion = (TrainMotion) w.get(k, trainID, principal);
		PositionOnTrack currentPosition = lastMotion.getFinalPosition();
		return currentPosition;
	}

	private Point currentTrainTarget(ReadOnlyWorld w) {
		TrainAccessor ta = new TrainAccessor(w, principal, trainID);
		return ta.getTarget();
	}

	SpeedAgainstTime nextSpeeds(ReadOnlyWorld w, Step v, GameTime t) {
		TrainAccessor ta = new TrainAccessor(w, principal, trainID);
		KEY k = ta.getLastKEY();
		TrainMotion lastMotion = (TrainMotion) w.get(k, trainID, principal);
		int u = lastMotion.getSpeed(t);
		int s = v.getLength();
		int wagons = ta.getTrain().getNumberOfWagons();
		int a0 = acceleration(wagons);
		int v1 = topSpeed(wagons);
		int t0 = t.getTicks();
		int t1 = ((v1 - u) / a0) + t0;

		// Over estimate the time to travel distance.
		GameTime pastEnd = new GameTime(s / v1 + t1 + 1);
		GameTime[] times = { t, new GameTime(t1), pastEnd };
		int[] speed = { u, v1, v1 };
		SpeedAgainstTime speeds = new SpeedAgainstTime(times, speed);
		// Find the time when we have just travelled the desired distance.
		GameTime end = speeds.getTime(s);
		SpeedAgainstTime clippedSpeeds = speeds.subSection(t, end);
		return clippedSpeeds;
	}

	TrainMotion nextMotion(ReadOnlyWorld w, Step v, GameTime t) {
		TrainAccessor ta = new TrainAccessor(w, principal, trainID);
		TrainMotion lastMotion = ta.findCurrentMotion(t);
		SpeedAgainstTime speeds = nextSpeeds(w, v, t);
		return lastMotion.next(speeds, v);
	}

	int acceleration(int wagons) {
		return 1;
	}

	int topSpeed(int wagons) {
		return 100 / wagons;
	}
}
