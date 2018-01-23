/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 *
 */
package freerails.move;

import freerails.util.Point2D;
import freerails.world.KEY;
import freerails.world.cargo.CargoBatch;
import freerails.world.cargo.MutableCargoBatchBundle;
import freerails.world.top.MapFixtureFactory;

/**
 *
 */
public class RemoveCargoBatchBundleMoveTest extends AbstractMoveTestCase {

    /**
     *
     */
    @Override
    public void testMove() {
        MutableCargoBatchBundle bundleA;
        MutableCargoBatchBundle bundleB;
        bundleA = new MutableCargoBatchBundle();
        bundleB = new MutableCargoBatchBundle();
        bundleA.setAmount(new CargoBatch(1, new Point2D(2, 3), 4, 0), 5);
        bundleB.setAmount(new CargoBatch(1, new Point2D(2, 3), 4, 0), 5);
        assertEquals(bundleA, bundleB);

        Move move = new RemoveCargoBundleMove(0, bundleB.toImmutableCargoBundle(),
                MapFixtureFactory.TEST_PRINCIPAL);
        assertSurvivesSerialisation(move);

        assertTryMoveFails(move);
        assertTryUndoMoveFails(move);
        getWorld().add(MapFixtureFactory.TEST_PRINCIPAL, KEY.CARGO_BUNDLES,
                bundleA.toImmutableCargoBundle());
        assertTryMoveIsOk(move);

        assertOkButNotRepeatable(move);
    }
}