/*
 * Created on 26-May-2003
 *
 */
package jfreerails.move;

import jfreerails.world.cargo.CargoBatch;
import jfreerails.world.cargo.MutableCargoBundle;
import jfreerails.world.top.KEY;
import jfreerails.world.top.MapFixtureFactory;

/**
 * JUnit test.
 * 
 * @author Luke
 * 
 */
public class AddCargoBundleMoveTest extends AbstractMoveTestCase {
    @Override
    public void testMove() {
        MutableCargoBundle bundleA;
        MutableCargoBundle bundleB;
        bundleA = new MutableCargoBundle();
        bundleB = new MutableCargoBundle();
        bundleA.setAmount(new CargoBatch(1, 2, 3, 4, 0), 5);
        bundleB.setAmount(new CargoBatch(1, 2, 3, 4, 0), 5);
        assertEquals(bundleA, bundleB);

        Move m = new AddCargoBundleMove(0, bundleA.toImmutableCargoBundle(),
                MapFixtureFactory.TEST_PRINCIPAL);
        assertDoMoveIsOk(m);
        assertEquals(getWorld().size(MapFixtureFactory.TEST_PRINCIPAL,
                KEY.CARGO_BUNDLES), 1);
        assertUndoMoveIsOk(m);
        assertSurvivesSerialisation(m);
        assertOkButNotRepeatable(m);
    }
}