/*
 * Created on 03-Feb-2005
 *
 */
package jfreerails.world.train;

import java.awt.Point;
import java.util.ArrayList;

import jfreerails.world.common.FreerailsPathIterator;
import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.GameTime;
import jfreerails.world.common.OneTileMoveVector;

/**
 * <p>
 * This immutable class provides methods that return a train's position and
 * speed at any time within an interval. An instance of this class will be
 * stored on the world object for each train rather the train’s position. The
 * reasons for this are as follows.
 * </p>
 * 
 * <p>
 * (1) It decouples the number of game updates per second and number of frames
 * per second shown by the client. If the train’s position were stored on the
 * world object, it would get updated each game tick. But this would mean that
 * if the game was being updated 10 times per second, even if the client was
 * displaying 50 FPS, the train’s motion would still appear jerky since its
 * position would only change 10 times per second.
 * </p>
 * <p>
 * (2) It makes supporting low bandwidth networks easier since it allows the
 * server to send updates less frequently.
 * </p>
 * 
 * 
 * @author Luke
 * @see jfreerails.world.train.PathOnTiles
 * @see jfreerails.world.train.SpeedAgainstTime
 */
public class TrainMotion implements FreerailsSerializable {

	private static final long serialVersionUID = 3618423722025891641L;

	private final int initialPosition;

	private final PathOnTiles path;

	private final SpeedAgainstTime speeds;

	private final int trainLength;

	/**
	 * Creates a new TrainMotion instance.
	 * 
	 * @param path
	 *            the path the train will take.
	 * @param enginePosition
	 *            the position measured in tiles that trains engine is along the
	 *            path
	 * @param trainLength
	 *            the length of the train, as returned by
	 *            <code>TrainModel.getLength()</code>.
	 * @throws IllegalArgumentException
	 *             if trainLength is out the range
	 *             <code>trainLength &gt; TrainModel.WAGON_LENGTH || trainLength &lt; TrainModel.MAX_TRAIN_LENGTH</code>
	 * @throws IllegalArgumentException
	 *             if
	 *             <code>path.getDistance(enginePosition) &lt; trainLength</code>.
	 * @throws IllegalArgumentException
	 *             if
	 *             <code>path.getDistance(enginePosition) + speeds.getDistance(speeds.getEnd))&lt;  path.getLength</code>.
	 */

	public TrainMotion(PathOnTiles path, int enginePosition, int trainLength,
			SpeedAgainstTime speeds) {
		if (trainLength < TrainModel.WAGON_LENGTH
				|| trainLength > TrainModel.MAX_TRAIN_LENGTH)
			throw new IllegalArgumentException();
		this.path = path;
		this.speeds = speeds;
		this.trainLength = trainLength;
		initialPosition = path.getDistance(enginePosition);
		if (initialPosition < trainLength)
			throw new IllegalArgumentException();
		if (path.getLength() < initialPosition
				+ speeds.getDistance(speeds.getEnd()))
			throw new IllegalArgumentException();
	}

	private int calcOffSet(GameTime t) {
		int offset = getDistance(t) + initialPosition - trainLength;
		return offset;
	}

	private void checkT(GameTime t) {
		if (t.getTime() < getStart().getTime())
			throw new IllegalArgumentException();
		if (t.getTime() > getEnd().getTime())
			throw new IllegalArgumentException();
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof TrainMotion))
			return false;

		final TrainMotion trainMotion = (TrainMotion) o;

		if (trainLength != trainMotion.trainLength)
			return false;
		if (!path.equals(trainMotion.path))
			return false;
		if (!speeds.equals(trainMotion.speeds))
			return false;

		return true;
	}

	/**
	 * Returns the train's distance along the track from the point the train was
	 * at at time <code>getStart()</code> at the specified time.
	 * 
	 * @param t
	 *            the time.
	 * @return the distance
	 * @throws IllegalArgumentException
	 *             if t is outside the interval
	 */
	public int getDistance(GameTime t) {
		checkT(t);
		return speeds.getDistance(t);
	}

	/** Returns the time at which the interval ends. */
	public GameTime getEnd() {
		return speeds.getEnd();
	}

	/**
	 * Returns the train's position at the specified time.
	 * 
	 * @param t
	 *            the time.
	 * @return the train's position.
	 * @throws IllegalArgumentException
	 *             if t is outside the interval
	 */
	public TrainPositionOnMap getPosition(GameTime t) {
		int offset = calcOffSet(t);
		FreerailsPathIterator pathIt = path.subPath(offset, trainLength);
		TrainPositionOnMap tpom = TrainPositionOnMap
				.createInSameDirectionAsPath(pathIt);
		return tpom.reverse();
	}

	/**
	 * Returns the train's speed in MPH at the specified time.
	 * 
	 * @param t
	 *            the time.
	 * @return the speed
	 * @throws IllegalArgumentException
	 *             if t is outside the interval
	 */
	public int getSpeed(GameTime t) {
		checkT(t);
		return speeds.getSpeed(t);
	}

	/** Returns the time at which the interval starts. */
	public GameTime getStart() {
		return speeds.getStart();
	}

	/**
	 * Returns an array of the tiles the train is on at the specified time.
	 * 
	 * @param t
	 *            the time.
	 * @return an array of the tiles the train is on
	 * @throws IllegalArgumentException
	 *             if t is outside the interval
	 */
	public Point[] getTiles(GameTime t) {
		checkT(t);
		int start = calcOffSet(t);
		int end = start + trainLength;
		ArrayList<Point> points = new ArrayList<Point>();
		int distanceSoFar = 0;
		Point p = path.getStart();
		for (int i = 0; i < path.steps(); i++) {
			OneTileMoveVector step = path.getStep(i);
			distanceSoFar += step.getLength();
			if (distanceSoFar > start) {
				points.add(new Point(p));
			}
			p.x += step.deltaX;
			p.y += step.deltaY;
			if (distanceSoFar >= end) {
				points.add(new Point(p));
				break;
			}
		}
		return points.toArray(new Point[points.size()]);
	}

	public int hashCode() {
		int result;
		result = path.hashCode();
		result = 29 * result + speeds.hashCode();
		result = 29 * result + trainLength;
		return result;
	}

	public TrainMotion next(SpeedAgainstTime newSpeeds,
			OneTileMoveVector[] newPathSection) {
		GameTime start = newSpeeds.getStart();
		Point[] tiles = getTiles(start);
		final int OLD = tiles.length - 1;
		OneTileMoveVector[] newPath = new OneTileMoveVector[newPathSection.length
				+ OLD];
		for (int i = 0; i < newPath.length; i++) {
			if (i < OLD) {
				Point a = tiles[i];
				Point b = tiles[i + 1];
				newPath[i] = OneTileMoveVector
						.getInstance(b.x - a.x, b.y - a.y);
			} else {
				newPath[i] = newPathSection[i - OLD];
			}
		}
		PathOnTiles pathOnTiles = new PathOnTiles(tiles[0], newPath);
		return new TrainMotion(pathOnTiles, pathOnTiles.getDistance(OLD),
				trainLength, newSpeeds);
	}

}
