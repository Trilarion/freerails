/*
 * Created on 03-Feb-2005
 *
 */
package jfreerails.world.train;

import java.awt.Point;

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

	private final PathOnTiles path;

	private final SpeedAgainstTime speeds;

	private final int trainLength;

	/**
	 * Creates a new TrainMotion instance.
	 * 
	 * @param path
	 *            the path the train will take.
	 * @param trainLength
	 *            the length of the train, as returned by
	 *            <code>TrainModel.getLength()</code>.
	 * @throws IllegalArgumentException
	 *             if trainLength is out the range
	 *             <code>length &gt; TrainModel.WAGON_LENGTH || length &lt; TrainModel.MAX_TRAIN_LENGTH</code>
	 * @throws IllegalArgumentException
	 *             if
	 *             <code>(initialPosition + speeds.getDistance(speeds.getEnd())) &lt; path.getLength()</code>.
	 */
	public TrainMotion(PathOnTiles path,  int trainLength,
			SpeedAgainstTime speeds) {
		this.path = path;
		this.speeds = speeds;	
		this.trainLength = trainLength;

	}
	
	private void checkT(GameTime t){
		if(t.getTime() < getStart().getTime()) throw new IllegalArgumentException();
		if(t.getTime() > getEnd().getTime()) throw new IllegalArgumentException();
	}

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TrainMotion)) return false;

        final TrainMotion trainMotion = (TrainMotion) o;

        if (trainLength != trainMotion.trainLength) return false;
        if (!path.equals(trainMotion.path)) return false;
        if (!speeds.equals(trainMotion.speeds)) return false;

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
		//Note, no need to call checkT() since it is called by getDistance(t)
		int offset = getDistance(t);
		FreerailsPathIterator pathIt = path.subPath(offset, trainLength);
		return TrainPositionOnMap.createInSameDirectionAsPath(pathIt);
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
		int distance = getDistance(t);
		int startIndex = path.getStepIndex(distance);
		int endIndex = path.getStepIndex(distance+ trainLength);
		Point[] returnValue = new Point[endIndex - startIndex +1];
		Point tile = path.getStart();
		for (int i = 0; i <= endIndex; i++) {
			OneTileMoveVector v = path.getStep(i);
			
			int j = i - startIndex;
			if(j >= 0){								
				returnValue[j] = new Point(tile);
				
			}
			tile.x += v.deltaX;
			tile.y += v.deltaY;
		}
		return returnValue;
	}

    public int hashCode() {
        int result;
        result = path.hashCode();
        result = 29 * result + speeds.hashCode();
        result = 29 * result + trainLength;
        return result;
    }

}
