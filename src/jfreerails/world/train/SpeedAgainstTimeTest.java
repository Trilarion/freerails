/*
 * Created on 04-Feb-2005
 *
 */
package jfreerails.world.train;

import jfreerails.world.common.GameTime;
import junit.framework.TestCase;

/**
 * JUnit test for SpeedAgainstTime.
 * 
 * @author Luke
 * 
 */
public class SpeedAgainstTimeTest extends TestCase {

	public void testSpeedAgainstTime() {
		GameTime[] times = { new GameTime(0), new GameTime(10) };
		int[] speeds = { 10, 20 };
		assertTrue("Expected NullPointerException", throwsException(null, null));
		assertTrue("Expected NullPointerException", throwsException(times,
				null));
		assertTrue("Expected NullPointerException", throwsException(null,
				speeds));

		assertFalse("Should be ok", throwsException(times, speeds));
		speeds = new int[] { -10, 20 };
		assertTrue(
				"Expected IllegalArgumentException because of negative speed values.",
				throwsException(times, speeds));
		speeds = new int[] { 10, 20, 30 };
		assertTrue("Expected IllegalArgumentException if times.length != speeds.length ", throwsException(times, speeds));
		
		
		 times = new GameTime[]{ new GameTime(0), new GameTime(10), new GameTime(5) };
		 assertTrue("Expected IllegalArgumentException if times[i] >= times[i + 1] for any i ", throwsException(times, speeds)); 
		 times = new GameTime[]{ new GameTime(0), new GameTime(10), null };
		 assertTrue("Expected IllegalArgumentException if any of the time values are null", throwsException(times, speeds));
		
		speeds = new int[0];
		times = new GameTime[0];
		assertTrue("IllegalArgumentException if times.length < 1",
				throwsException(times, speeds));

	}
	
	public void testGetAcceleration(){
		GameTime[] times = { new GameTime(0), new GameTime(10) };
		int[] speeds = { 10, 20 };
		SpeedAgainstTime sat = new SpeedAgainstTime(times, speeds);		
		assertEquals(1, sat.getAcceleration(new GameTime(0)));
		assertEquals(1, sat.getAcceleration(new GameTime(1)));
		assertEquals(1, sat.getAcceleration(new GameTime(9)));
		
		times = new GameTime[] { new GameTime(0), new GameTime(10), new GameTime(15) };
		speeds = new int[]{ 10, 20, 10 };
		sat = new SpeedAgainstTime(times, speeds);	
		assertEquals(1, sat.getAcceleration(new GameTime(0)));
		assertEquals(1, sat.getAcceleration(new GameTime(1)));
		assertEquals(1, sat.getAcceleration(new GameTime(9)));
		
		assertEquals(-2, sat.getAcceleration(new GameTime(10)));
		assertEquals(-2, sat.getAcceleration(new GameTime(11)));
		assertEquals(-2, sat.getAcceleration(new GameTime(14)));
		
	}
	
	public void testGetSpeed(){
		GameTime[] times = { new GameTime(0), new GameTime(10) };
		int[] speeds = { 10, 20 };
		SpeedAgainstTime sat = new SpeedAgainstTime(times, speeds);		
		assertEquals(10, sat.getSpeed(new GameTime(0)));
		assertEquals(20, sat.getSpeed(new GameTime(10)));
		assertEquals(15, sat.getSpeed(new GameTime(5)));
	}
	
	public void testGetDistanceAndGetTime(){		
		GameTime[] times = new GameTime[] { new GameTime(0), new GameTime(10), new GameTime(15) };
		int[] speeds = new int[]{ 10, 20, 10 };
		
		SpeedAgainstTime sat = new SpeedAgainstTime(times, speeds);
		
		//Check distance at the 'fence post' points
		assertEquals(0, sat.getDistance(new GameTime(0)));		
		assertEquals(150, sat.getDistance(new GameTime(10)));
		assertEquals(150+75, sat.getDistance(new GameTime(15)));
		
		//Check distance in middle of intervals.
		assertEquals(78, sat.getDistance(new GameTime(6)));
		assertEquals(150 + 36, sat.getDistance(new GameTime(12)));
		
		//Check distance at the 'fence post' points
		assertEquals(new GameTime(0), sat.getTime(0));		
		assertEquals(new GameTime(10), sat.getTime(150));
		assertEquals(new GameTime(15), sat.getTime(150+75));
		
		//Check distance in middle of intervals.
		assertEquals(new GameTime(6), sat.getTime(78));
		assertEquals(new GameTime(12), sat.getTime(150 + 36));
	}
		

	private boolean throwsException(GameTime[] times, int[] speeds) {
		try {
			new SpeedAgainstTime(times, speeds);
			return false;
		} catch (Exception e) {
			return true;
		}

	}

}
