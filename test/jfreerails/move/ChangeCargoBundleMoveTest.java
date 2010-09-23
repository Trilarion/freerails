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
public class ChangeCargoBundleMoveTest extends AbstractMoveTestCase {
	@Override
	public void testMove() {
		MutableCargoBundle before;
		MutableCargoBundle after;
		before = new MutableCargoBundle();
		after = new MutableCargoBundle();
		before.setAmount(new CargoBatch(1, 2, 3, 4, 0), 5);
		after.setAmount(new CargoBatch(1, 2, 3, 4, 0), 8);

		Move m = new ChangeCargoBundleMove(before.toImmutableCargoBundle(),
				after.toImmutableCargoBundle(), 0,
				MapFixtureFactory.TEST_PRINCIPAL);
		assertSurvivesSerialisation(m);

		assertTryMoveFails(m);
		assertTryUndoMoveFails(m);
		getWorld().add(MapFixtureFactory.TEST_PRINCIPAL, KEY.CARGO_BUNDLES,
				before.toImmutableCargoBundle());
	}
}