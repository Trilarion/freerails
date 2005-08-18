/*
 * Created on 18-Jul-2005
 *
 */
package jfreerails.world.train;

import junit.framework.TestCase;

public class CompositeSpeedAgainstTimeTest extends TestCase {
	
	public void testBounds(){
		SpeedAgainstTime sat = ConstAcc.uas(10, 2, 400d);
		CompositeSpeedAgainstTime csat = new CompositeSpeedAgainstTime(sat);
		double t = csat.duration();
		double t2 = csat.calcT(400d);
		assertEquals(t, t2);
		double s = csat.calcS(t);
		assertEquals(400d, s);
		double t3 = csat.calcT(0d);
		assertEquals(0d, t3);
	}
	
	

}
