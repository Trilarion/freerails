/*
 * Created on 26-May-2003
 *
 */
package freerails.move;

import freerails.world.cargo.CargoBatch;
import freerails.world.cargo.MutableCargoBundle;
import freerails.world.top.KEY;
import freerails.world.top.MapFixtureFactory;

/**
 * JUnit test.
 *
 */
public class AddCargoBundleMoveTest extends AbstractMoveTestCase {

    /**
     *
     */
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