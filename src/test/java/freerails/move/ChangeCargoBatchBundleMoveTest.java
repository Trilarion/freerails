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
import freerails.world.MapFixtureFactory;

/**
 *
 */
public class ChangeCargoBatchBundleMoveTest extends AbstractMoveTestCase {

    /**
     *
     */
    @Override
    public void testMove() {
        MutableCargoBatchBundle before;
        MutableCargoBatchBundle after;
        before = new MutableCargoBatchBundle();
        after = new MutableCargoBatchBundle();
        before.setAmount(new CargoBatch(1, new Point2D(2, 3), 4, 0), 5);
        after.setAmount(new CargoBatch(1, new Point2D(2, 3), 4, 0), 8);

        Move move = new ChangeCargoBundleMove(before.toImmutableCargoBundle(),
                after.toImmutableCargoBundle(), 0,
                MapFixtureFactory.TEST_PRINCIPAL);
        assertSurvivesSerialisation(move);

        assertTryMoveFails(move);
        assertTryUndoMoveFails(move);
        getWorld().add(MapFixtureFactory.TEST_PRINCIPAL, KEY.CARGO_BUNDLES,
                before.toImmutableCargoBundle());
    }
}