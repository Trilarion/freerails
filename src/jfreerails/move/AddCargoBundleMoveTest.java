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
 * JUnit test.
 * @author Luke
 *
 */
public class AddCargoBundleMoveTest extends AbstractMoveTestCase {
    public void testMove() {
        CargoBundle bundleA;
        CargoBundle bundleB;
        bundleA = new CargoBundleImpl();
        bundleB = new CargoBundleImpl();
        bundleA.setAmount(new CargoBatch(1, 2, 3, 4, 0), 5);
        bundleB.setAmount(new CargoBatch(1, 2, 3, 4, 0), 5);
        assertEquals(bundleA, bundleB);

        Move m = new AddCargoBundleMove(0, bundleA,
                MapFixtureFactory.TEST_PRINCIPAL);
        assertDoMoveIsOk(m);
        assertEquals(getWorld().size(KEY.CARGO_BUNDLES,
                MapFixtureFactory.TEST_PRINCIPAL), 1);
        assertUndoMoveIsOk(m);
        assertEqualsSurvivesSerialisation(m);
        assertOkButNotRepeatable(m);
    }
}