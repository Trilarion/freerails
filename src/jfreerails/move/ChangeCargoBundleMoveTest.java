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
public class ChangeCargoBundleMoveTest extends AbstractMoveTestCase {
    public void testMove() {
        CargoBundle before;
        CargoBundle after;
        before = new CargoBundleImpl();
        after = new CargoBundleImpl();
        before.setAmount(new CargoBatch(1, 2, 3, 4, 0), 5);
        after.setAmount(new CargoBatch(1, 2, 3, 4, 0), 8);

        Move m = new ChangeCargoBundleMove(before, after, 0,
                MapFixtureFactory.TEST_PRINCIPAL);
        assertEqualsSurvivesSerialisation(m);

        assertTryMoveFails(m);
        assertTryUndoMoveFails(m);
        getWorld().add(KEY.CARGO_BUNDLES, before,
            MapFixtureFactory.TEST_PRINCIPAL);
    }
}