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
 * Created on 26-May-2003
 *
 */
package freerails.move;

import freerails.world.KEY;
import freerails.world.cargo.CargoBatch;
import freerails.world.cargo.MutableCargoBundle;
import freerails.world.top.MapFixtureFactory;

/**
 * JUnit test.
 */
public class RemoveCargoBundleMoveTest extends AbstractMoveTestCase {

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