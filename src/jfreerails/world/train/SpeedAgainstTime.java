/*
 * Created on 03-Feb-2005
 *
 */
package jfreerails.world.train;

import java.util.Arrays;

import jfreerails.util.Utils;
import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.GameTime;

/**
 * <p>
 * This class stores the values you could use to draw a graph of speed against
 * time.  You specify the speed of an entity at certain points in time. Between these times,
 *  the entity is assumed to be accelerating at a constant rate.
 * </p>
 * 
 * <p>
 * Example usage
 * </p>
 * <code><pre>
 * GameTime[] times = { new GameTime(0), new GameTime(10) };
 * int[] speeds = { 0, 20 };
 * SpeedAgainstTime sat = new SpeedAgainstTime(times, speeds);
 * int speed = sat.getSpeed(new GameTime(0)); //speed == 0;
 * speed = sat.getSpeed(new GameTime(5)); //speed == 10;
 * speed = sat.getSpeed(new GameTime(10)); //speed == 20;
 * int acceleration = sat.getAcceleration(new GameTime(7)); //acceleration == 2
 * 
 * 
 * </pre>
 * </code>
 * 
 * @author Luke
 * 
 */
public class SpeedAgainstTime implements FreerailsSerializable {

	private static final long serialVersionUID = 3618423722025891641L;
	
	public static final SpeedAgainstTime STOPPED = new SpeedAgainstTime(new GameTime[]{GameTime.BIG_BANG, GameTime.END_OF_THE_WORLD}, new int[]{0, 0});

	private final int[] speeds;

    private final GameTime[] times;

	/**
	 * @param times
	 * @param speeds
	 * @throws NullPointerException
	 *             if(null == times || null == speeds)
	 * @throws IllegalArgumentException
	 *             if times.length < 2
	 * @throws IllegalArgumentException
	 *             if times.length != speeds.length
	 * @throws IllegalArgumentException
	 *             if any of the speed values are negative.
	 * @throws IllegalArgumentException
	 *             if times[i] >= times[i + 1] for any i.
	 * @throws NullPointerException
	 *             if any of the time values are null.
	 * 
	 */
	public SpeedAgainstTime(GameTime[] times, int[] speeds) {
		if (null == times || null == speeds)
			throw new NullPointerException();
		if (times.length != speeds.length)
			throw new IllegalArgumentException();
		if (times.length < 1)
			throw new IllegalArgumentException("times.length < 2");
		for (int speed : speeds) {
			if (speed < 0)
				throw new IllegalArgumentException("speed < 0");
		}
		for (int i = 1; i < times.length; i++) {
			if (times[i - 1].getTime() >= times[i].getTime())
				throw new IllegalArgumentException(
						"IllegalArgumentException if times[i] >= times[i + 1] for any i.");
		}
		this.times = times;
		this.speeds = speeds;
	}

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SpeedAgainstTime)) return false;

        final SpeedAgainstTime speedAgainstTime = (SpeedAgainstTime) o;

        if (!Arrays.equals(speeds, speedAgainstTime.speeds)) return false;
        if (!Arrays.equals(times, speedAgainstTime.times)) return false;

        return true;
    }

	public int getAcceleration(GameTime t) {
		for (int i = 1; i < times.length; i++) {
			int start = times[i - 1].getTime();
			int end = times[i].getTime();
			if (start <= t.getTime() && end > t.getTime()) {
				int dSpeed = speeds[i] - speeds[i - 1];
				int dt = end - start;
				return dSpeed / dt;
			}
		}
		throw new IllegalArgumentException(String.valueOf(t));
	}

	public int getDistance(GameTime t) {
		int s = 0;//distance travelled.
		int time = t.getTime();
		for (int i = 1; i < times.length; i++) {
			int start = times[i - 1].getTime();
			int end = times[i].getTime();						
			
			if(time == start) return s;
			assert(start < time);
			
			if(time >= end){	
				int dt = end - start;
				s+= (speeds[i] + speeds[i - 1]) * dt / 2;
				if(time == end) return s;
			}else{
				int dt = time - start;
				int a = getAcceleration(t);
				s+=  speeds[i - 1] * dt + a * dt * dt / 2; //s=ut+0.5at^2
				return s;
			}
		}		
		throw new IllegalArgumentException(String.valueOf(t));
	}

	public GameTime getEnd() {
		return times[times.length - 1];
	}

	public int getSpeed(GameTime t) {
		int time = t.getTime();
		for (int i = 1; i < times.length; i++) {
			int start = times[i - 1].getTime();
			int end = times[i].getTime();						
			
			if(time == start) return speeds[i-1];
			if(time == end) return speeds[i];
			
			if (start < time && end >= time) {
				int dSpeed = speeds[i] - speeds[i - 1];
				int dt = end - start;
				int speed = speeds[i-1];
				speed += dSpeed * (time - start) / dt;
				return speed;
			}
		}
		throw new IllegalArgumentException(String.valueOf(t));
	}

	public GameTime getStart() {
		return times[0];
	}
	
	public GameTime getTime(int distance){
		int s = 0;//distance travelled.
	
		for (int i = 1; i < times.length; i++) {
			int start = times[i - 1].getTime();
			int end = times[i].getTime();		
			if(distance == s) return times[i - 1];
			assert(s < distance);
			int dt = end - start;
			int ds = (speeds[i] + speeds[i - 1]) * dt / 2;
			if(distance >= s + ds){
				s += ds;
				if(distance == s) return times[i];
			}else{
				//Solve s = ut + 0.5at^2 for t.
				int acc = (speeds[i] - speeds[i - 1]) / (end - start);
				double a = acc * 0.5d;
				double b = speeds[i - 1];
				double c = s - distance;
				double t = Utils.solveQuadratic(a, b, c);
				return new GameTime(start + (int)Math.round(t));
			}						
		}		
		throw new IllegalArgumentException(String.valueOf(distance));
		
	}

    public int hashCode() {
        return speeds.length;
    }
	
	public SpeedAgainstTime subSection(GameTime from, GameTime to){
		int arraySize = 2; //the minimum size.
		for(int i = 0; i < times.length; i++){
			int t = times[i].getTime();
			if(t > from.getTime() && t < to.getTime()){
				arraySize++;
			}
		}
		
		GameTime[] newTimes = new GameTime[arraySize];
		int[] newSpeeds = new int [arraySize];

		newTimes[0]= from;
		newTimes[arraySize-1]= to;
		newSpeeds[0] = getSpeed(from);
		newSpeeds[arraySize-1] = getSpeed(to);
		int j = 1;
		for(int i = 0; i < times.length; i++){
			int t = times[i].getTime();
			if(t > from.getTime() && t < to.getTime()){
				newTimes[j]= times[i];				
				newSpeeds[j] = speeds[j];				
				j++;
			}
		}
		
		return new SpeedAgainstTime(newTimes, newSpeeds);
	}

}
