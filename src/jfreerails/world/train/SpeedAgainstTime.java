/*
 * Created on 03-Feb-2005
 *
 */
package jfreerails.world.train;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.GameTime;

/**
 * <p>
 * This class stores the values you could use to draw a graph of speed against
 * time.
 * </p>
 * 
 * <p>
 * Example usage
 * </p>
 * <code><pre>
 GameTime[] times = {new GameTime(0), new GameTime(10)};
 int[] speeds = {0, 20};		
 SpeedAgainstTime sat = new SpeedAgainstTime(times, speeds);
 int speed = sat.getSpeed(new GameTime(0));  //speed == 0;
 speed = sat.getSpeed(new GameTime(5)); //speed == 10;
 speed = sat.getSpeed(new GameTime(10)); //speed == 20;
 int acceleration = sat.getAcceleration(new GameTime(7)); //acceleration == 2
 </pre>
 * </code>
 * 
 * @author Luke
 * 
 */
public class SpeedAgainstTime implements FreerailsSerializable {

	private static final long serialVersionUID = 3618423722025891641L;

	private final GameTime[] times;

	private final int[] speeds;

	/**
	 * @param times
	 * @param speeds
	 * @throws IllegalArgumentException if times.length != speeds.length 
	 * @throws IllegalArgumentException if any of the speed values are negative. 
	 * @throws IllegalArgumentException if times[i] >= times[i + 1] for any i
	 * @throws IllegalArgumentException if times[i] == null for any i
	 * 
	 */
	public SpeedAgainstTime(GameTime[] times, int[] speeds) {
		this.times = times;
		this.speeds = speeds;
	}

	public int getAcceleration(GameTime t) {
		return 0;
	}

	public int getSpeed(GameTime t) {
		return 0;
	}

	public int getDistance(GameTime t) {

		return 0;
	}

	public GameTime getStart() {
		return times[0];
	}

	public GameTime getEnd() {
		return times[times.length - 1];
	}

}
