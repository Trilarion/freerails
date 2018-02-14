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

import freerails.move.listmove.AddCargoBundleMove;
import freerails.util.Vector2D;
import freerails.world.KEY;
import freerails.world.cargo.CargoBatch;
import freerails.world.cargo.MutableCargoBatchBundle;
import freerails.world.MapFixtureFactory;

/**
 *
 */
public class AddCargoBatchBundleMoveTest extends AbstractMoveTestCase {

    /**
     *
     */
    public void testMove() {
        MutableCargoBatchBundle bundleA;
        MutableCargoBatchBundle bundleB;
        bundleA = new MutableCargoBatchBundle();
        bundleB = new MutableCargoBatchBundle();
        bundleA.setAmount(new CargoBatch(1, new Vector2D(2, 3), 4, 0), 5);
        bundleB.setAmount(new CargoBatch(1, new Vector2D(2, 3), 4, 0), 5);
        assertEquals(bundleA, bundleB);

        Move move = new AddCargoBundleMove(0, bundleA.toImmutableCargoBundle(), MapFixtureFactory.TEST_PRINCIPAL);
        assertDoMoveIsOk(move);
        assertEquals(getWorld().size(MapFixtureFactory.TEST_PRINCIPAL, KEY.CARGO_BUNDLES), 1);
        assertUndoMoveIsOk(move);
        assertSurvivesSerialisation(move);
        assertOkButNotRepeatable(move);
    }
}