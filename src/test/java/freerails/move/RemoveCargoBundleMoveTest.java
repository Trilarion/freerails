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
 * @author Luke
 */
public class RemoveCargoBundleMoveTest extends AbstractMoveTestCase {
    @Override
    public void testMove() {
        MutableCargoBundle bundleA;
        MutableCargoBundle bundleB;
        bundleA = new MutableCargoBundle();
        bundleB = new MutableCargoBundle();
        bundleA.setAmount(new CargoBatch(1, 2, 3, 4, 0), 5);
        bundleB.setAmount(new CargoBatch(1, 2, 3, 4, 0), 5);
        assertEquals(bundleA, bundleB);

        Move m = new RemoveCargoBundleMove(0, bundleB.toImmutableCargoBundle(),
                MapFixtureFactory.TEST_PRINCIPAL);
        assertSurvivesSerialisation(m);

        assertTryMoveFails(m);
        assertTryUndoMoveFails(m);
        getWorld().add(MapFixtureFactory.TEST_PRINCIPAL, KEY.CARGO_BUNDLES,
                bundleA.toImmutableCargoBundle());
        assertTryMoveIsOk(m);

        assertOkButNotRepeatable(m);
    }
}