/*
 * Created on 26-May-2003
 *
 */
package jfreerails.move;

import jfreerails.world.cargo.CargoBatch;
import jfreerails.world.cargo.CargoBundle;
import jfreerails.world.cargo.CargoBundleImpl;
import jfreerails.world.top.KEY;
import jfreerails.world.top.MapFixtureFactory;


/**
 *  JUnit test.
 * @author Luke
 *
 */
public class RemoveCargoBundleMoveTest extends AbstractMoveTestCase {
    public void testMove() {
        CargoBundle bundleA;
        CargoBundle bundleB;
        bundleA = new CargoBundleImpl();
        bundleB = new CargoBundleImpl();
        bundleA.setAmount(new CargoBatch(1, 2, 3, 4, 0), 5);
        bundleB.setAmount(new CargoBatch(1, 2, 3, 4, 0), 5);
        assertEquals(bundleA, bundleB);

        Move m = new RemoveCargoBundleMove(0, bundleB,
                MapFixtureFactory.TEST_PRINCIPAL);
        assertEqualsSurvivesSerialisation(m);

        assertTryMoveFails(m);
        assertTryUndoMoveFails(m);
        getWorld().add(KEY.CARGO_BUNDLES, bundleA,
            MapFixtureFactory.TEST_PRINCIPAL);
        assertTryMoveIsOk(m);

        assertOkButNotRepeatable(m);
    }
}