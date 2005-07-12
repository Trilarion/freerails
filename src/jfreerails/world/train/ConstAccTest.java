/*
 * Created on 09-Jul-2005
 *
 */
package jfreerails.world.train;

import junit.framework.TestCase;

public class ConstAccTest extends TestCase {

	public void testTandS() {
		SpeedAgainstTime acc1 = ConstAcc.uat(0, 10, 5);
		double s = acc1.getS();
		SpeedAgainstTime acc2 = ConstAcc.uas(0, 10, s);
		assertEquals(acc1, acc2);

		acc1 = ConstAcc.uat(10, 0, 5);
		assertEquals(50, acc1.getS(), 0.00001);
		acc2 = ConstAcc.uas(10, 0, acc1.getS());
		assertEquals(acc1, acc2);

	}

	public void testEquals() {
		SpeedAgainstTime acc1 = ConstAcc.uat(0, 10, 4);
		SpeedAgainstTime acc2 = ConstAcc.uat(0, 10, 4);
		assertEquals(acc1, acc2);
	}

}
