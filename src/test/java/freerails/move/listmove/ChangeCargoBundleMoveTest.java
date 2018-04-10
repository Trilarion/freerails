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
package freerails.move.listmove;

import freerails.move.AbstractMoveTestCase;
import freerails.move.Move;
import freerails.util.Vec2D;
import freerails.model.world.PlayerKey;
import freerails.model.cargo.CargoBatch;
import freerails.model.cargo.MutableCargoBatchBundle;
import freerails.model.MapFixtureFactory;

/**
 *
 */
public class ChangeCargoBundleMoveTest extends AbstractMoveTestCase {

    /**
     *
     */
    public void testMove() {
        MutableCargoBatchBundle before;
        MutableCargoBatchBundle after;
        before = new MutableCargoBatchBundle();
        after = new MutableCargoBatchBundle();
        before.setAmount(new CargoBatch(1, new Vec2D(2, 3), 4, 0), 5);
        after.setAmount(new CargoBatch(1, new Vec2D(2, 3), 4, 0), 8);

        Move move = new ChangeCargoBundleMove(before.toImmutableCargoBundle(),
                after.toImmutableCargoBundle(), 0, MapFixtureFactory.TEST_PRINCIPAL);
        assertSurvivesSerialisation(move);

        assertTryMoveFails(move);
        assertTryUndoMoveFails(move);
        getWorld().add(MapFixtureFactory.TEST_PRINCIPAL, PlayerKey.CargoBundles, before.toImmutableCargoBundle());
    }
}