/*
 * Created on 26-May-2003
 *
 */
package jfreerails.move;

import jfreerails.world.cargo.CargoBatch;
import jfreerails.world.cargo.CargoBundle;
import jfreerails.world.cargo.CargoBundleImpl;
import jfreerails.world.top.KEY;


/**
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

        Move m = new ChangeCargoBundleMove(before, after, 0);
        assertEqualsSurvivesSerialisation(m);

        assertTryMoveFails(m);
        assertTryUndoMoveFails(m);
        world.add(KEY.CARGO_BUNDLES, before);
    }
}