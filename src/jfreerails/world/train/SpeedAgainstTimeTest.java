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
		assertTrue("Expected NullPointerException",
				throwsException(times, null));
		assertTrue("Expected NullPointerException", throwsException(null,
				speeds));

		assertFalse("Should be ok", throwsException(times, speeds));
		speeds = new int[] { -10, 20 };
		assertTrue(
				"Expected IllegalArgumentException because of negative speed values.",
				throwsException(times, speeds));
		speeds = new int[] { 10, 20, 30 };
		assertTrue(
				"Expected IllegalArgumentException if times.length != speeds.length ",
				throwsException(times, speeds));

		times = new GameTime[] { new GameTime(0), new GameTime(10),
				new GameTime(5) };
		assertTrue(
				"Expected IllegalArgumentException if times[i] >= times[i + 1] for any i ",
				throwsException(times, speeds));
		times = new GameTime[] { new GameTime(0), new GameTime(10), null };
		assertTrue(
				"Expected IllegalArgumentException if any of the time values are null",
				throwsException(times, speeds));

		speeds = new int[0];
		times = new GameTime[0];
		assertTrue("IllegalArgumentException if times.length < 1",
				throwsException(times, speeds));

	}

	public void testGetAcceleration() {
		GameTime[] times = { new GameTime(0), new GameTime(10) };
		int[] speeds = { 10, 20 };
		SpeedAgainstTime sat = new SpeedAgainstTime(times, speeds);
		assertEquals(1, sat.getAcceleration(new GameTime(0)));
		assertEquals(1, sat.getAcceleration(new GameTime(1)));
		assertEquals(1, sat.getAcceleration(new GameTime(9)));

		times = new GameTime[] { new GameTime(0), new GameTime(10),
				new GameTime(15) };
		speeds = new int[] { 10, 20, 10 };
		sat = new SpeedAgainstTime(times, speeds);
		assertEquals(1, sat.getAcceleration(new GameTime(0)));
		assertEquals(1, sat.getAcceleration(new GameTime(1)));
		assertEquals(1, sat.getAcceleration(new GameTime(9)));

		assertEquals(-2, sat.getAcceleration(new GameTime(10)));
		assertEquals(-2, sat.getAcceleration(new GameTime(11)));
		assertEquals(-2, sat.getAcceleration(new GameTime(14)));

	}

	public void testGetSpeed() {
		GameTime[] times = { new GameTime(0), new GameTime(10) };
		int[] speeds = { 10, 20 };
		SpeedAgainstTime sat = new SpeedAgainstTime(times, speeds);
		assertEquals(10, sat.getSpeed(new GameTime(0)));
		assertEquals(20, sat.getSpeed(new GameTime(10)));
		assertEquals(15, sat.getSpeed(new GameTime(5)));
	}

	public void testStopped() {
		SpeedAgainstTime stopped = SpeedAgainstTime.STOPPED;
		assertEquals(0, stopped.getSpeed(new GameTime(0)));
		assertEquals(0, stopped.getSpeed(new GameTime(10)));
		assertEquals(0, stopped.getSpeed(new GameTime(5)));

		assertEquals(0, stopped.getAcceleration(new GameTime(10)));
		assertEquals(0, stopped.getAcceleration(new GameTime(11)));
		assertEquals(0, stopped.getAcceleration(new GameTime(14)));

		assertEquals(0, stopped.getDistance(new GameTime(6)));
		assertEquals(0, stopped.getDistance(new GameTime(12)));

	}

	public void testSubSection() {
		GameTime[] times = new GameTime[] { new GameTime(0), new GameTime(10),
				new GameTime(15) };
		int[] speeds = new int[] { 10, 20, 10 };
		SpeedAgainstTime sat = new SpeedAgainstTime(times, speeds);
		SpeedAgainstTime sub1 = sat.subSection(new GameTime(0),
				new GameTime(15));
		assertEquals(sat, sub1);
		SpeedAgainstTime sub2 = sat.subSection(new GameTime(0),
				new GameTime(10));
		assertFalse(sat.equals(sub2));
		assertFalse(sub1.equals(sub2));
		times = new GameTime[] { new GameTime(0), new GameTime(10) };
		speeds = new int[] { 10, 20 };
		SpeedAgainstTime expected2 = new SpeedAgainstTime(times, speeds);
		assertEquals(sub2, expected2);
	}

	public void testEquals() {
		GameTime[] times = new GameTime[] { new GameTime(0), new GameTime(10),
				new GameTime(15) };
		int[] speeds = new int[] { 10, 20, 10 };
		SpeedAgainstTime sat0 = new SpeedAgainstTime(times, speeds);
		SpeedAgainstTime sat1 = new SpeedAgainstTime(times, speeds);
		assertEquals(sat0, sat0);
		assertEquals(sat0, sat1);

		times = new GameTime[] { new GameTime(0), new GameTime(10),
				new GameTime(15) };
		speeds = new int[] { 10, 20, 11 };

		SpeedAgainstTime sat3 = new SpeedAgainstTime(times, speeds);
		assertFalse(sat0.equals(sat3));

	}

	public void testGetDistanceAndGetTime() {
		GameTime[] times = new GameTime[] { new GameTime(0), new GameTime(10),
				new GameTime(15) };
		int[] speeds = new int[] { 10, 20, 10 };

		SpeedAgainstTime sat = new SpeedAgainstTime(times, speeds);

		// Check distance at the 'fence post' points
		assertEquals(0, sat.getDistance(new GameTime(0)));
		assertEquals(150, sat.getDistance(new GameTime(10)));
		assertEquals(150 + 75, sat.getDistance(new GameTime(15)));

		// Check distance in middle of intervals.
		assertEquals(78, sat.getDistance(new GameTime(6)));
		assertEquals(150 + 36, sat.getDistance(new GameTime(12)));

		// Check distance at the 'fence post' points
		assertEquals(new GameTime(0), sat.getTime(0));
		assertEquals(new GameTime(10), sat.getTime(150));
		assertEquals(new GameTime(15), sat.getTime(150 + 75));

		// Check distance in middle of intervals.
		assertEquals(new GameTime(6), sat.getTime(78));
		assertEquals(new GameTime(12), sat.getTime(150 + 36));

		// Should throw an ArithmeticException since GameTime.END_OF_THE_WORLD *
		// 10 > Integer.MAX_VALUE
		try {
			times = new GameTime[] { new GameTime(0), GameTime.END_OF_THE_WORLD };
			speeds = new int[] { 10, 10 };
			@SuppressWarnings("unused") SpeedAgainstTime newSpeeds;
			newSpeeds = new SpeedAgainstTime(times, speeds);
			fail();
		} catch (ArithmeticException e) {
			// This is expected, so ok.
		}

	}
	
	public void testGetActivity(){
		
		SpeedTimeAndStatus[] points = {new SpeedTimeAndStatus(new GameTime(0), 5, SpeedTimeAndStatus.Activity.READY),
				new SpeedTimeAndStatus(new GameTime(10), 0, SpeedTimeAndStatus.Activity.STOPPED_AT_SIGNAL),
				new SpeedTimeAndStatus(new GameTime(25), 0, SpeedTimeAndStatus.Activity.NEEDS_UPDATING)};
		SpeedAgainstTime newSpeeds = new SpeedAgainstTime(points);
		assertEquals(SpeedTimeAndStatus.Activity.READY, newSpeeds.getActivity(new GameTime(0)));
		assertEquals(SpeedTimeAndStatus.Activity.READY, newSpeeds.getActivity(new GameTime(9)));
		
		assertEquals(SpeedTimeAndStatus.Activity.STOPPED_AT_SIGNAL, newSpeeds.getActivity(new GameTime(10)));
		assertEquals(SpeedTimeAndStatus.Activity.STOPPED_AT_SIGNAL, newSpeeds.getActivity(new GameTime(24)));
		
		assertEquals(SpeedTimeAndStatus.Activity.NEEDS_UPDATING, newSpeeds.getActivity(new GameTime(25)));
		
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
