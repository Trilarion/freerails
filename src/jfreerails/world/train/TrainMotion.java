/*
 * Created on 03-Feb-2005
 *
 */
package jfreerails.world.train;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.GameTime;
import jfreerails.world.common.PositionOnTrack;

/**
 * <p>
 * This immutable class provides methods that return a train's position and speed at any
 * time within an interval. An instance of this class will be stored on the
 * world object for each train rather the train’s position. The reasons for this
 * are as follows.
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

	private  final SpeedAgainstTime speeds;
	
	private final  int initialPosition;
	
	private final int trainLength;
	
	
	/**
	 * Creates a new TrainMotion instance.
	 * @param path the path the train will take.
	 * @param initialPosition the distance the trains engine is along the path at time <code>speeds.getStart()</code>.
	 * @param trainLength the length of the train, as returned by <code>TrainModel.getLength()</code>.		
	 * @throws IllegalArgumentException if length is out the range 
	 * <code>length &gt; TrainModel.WAGON_LENGTH || length &lt; TrainModel.MAX_TRAIN_LENGTH</code> 
	 * @throws IllegalArgumentException if <code> initialPosition &lt; length</code>.
	 * @throws IllegalArgumentException if <code>(initialPosition + speeds.getDistance(speeds.getEnd())) &lt; path.getLength()</code>.
	 */	
	public TrainMotion(PathOnTiles path, int initialPosition, int trainLength, SpeedAgainstTime speeds){
		this.path =path;
		this.speeds = speeds;
		this.initialPosition = initialPosition;
		this.trainLength = trainLength;	
		
	}

	/**
	 * Returns the train's position at the specified time.  
	 * @param t the time.	
	 * @return the train's position
	 * @throws IllegalArgumentException if t is outside the interval
	 * (<tt>t &gt; from || t &lt; to</tt>) or length is out the range 
	 * (<tt>length &gt; TrainModel.WAGON_LENGTH || length &lt; TrainModel.MAX_TRAIN_LENGTH</tt>) 
	 */
	public TrainPositionOnMap getPosition(GameTime t) {
		return null;
	}

	/**
	 * Returns an array of the tiles the train is on at the specified time.  
	 * @param t the time.	
	 * @return an array of the tiles the train is on
	 * @throws IllegalArgumentException if t is outside the interval
	 * (<tt>t &gt; from || t &lt; to</tt>) or length is out the range 	 
	 */
	public PositionOnTrack[] getTiles(GameTime t) {
		return null;
	}

	
	/**
	 * Returns the train's speed in MPH at the specified time.  
	 * @param t the time.	
	 * @return the speed
	 * @throws IllegalArgumentException if t is outside the interval
	 * (<tt>t &gt; from || t &lt; to</tt>) or length is out the range 	  
	 */
	public int getSpeed(GameTime t) {
		return 0;
	}

	/**
	 * Returns the train's distance along the track from the point the train was at at time
	 * <code>from</code> at the specified time.  
	 * @param t the time.	
	 * @return the distance
	 * @throws IllegalArgumentException if t is outside the interval
	 * (<tt>t &gt; from || t &lt; to</tt>) or length is out the range 	  
	 */
	public int getDistance(GameTime t) {
		return 0;
	}

}
