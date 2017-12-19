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

package freerails.move;

import freerails.world.top.World;
import freerails.world.top.WorldDiffs;
import freerails.world.track.FreerailsTile;

/**
 * JUnit test for MapDiffMove.
 *
 */
public class MapDiffMoveTest extends AbstractMoveTestCase {

    /**
     *
     */
    @Override
    public void testMove() {
        World world2 = this.getWorld();
        WorldDiffs worldDiff = new WorldDiffs(world2);

        FreerailsTile tile = (FreerailsTile) world2.getTile(2, 2);
        assertNotNull(tile);
        assertEquals(tile, worldDiff.getTile(2, 2));

        FreerailsTile newTile = FreerailsTile.getInstance(999);
        worldDiff.setTile(3, 5, newTile);
        assertEquals(newTile, worldDiff.getTile(3, 5));

        Move m = new WorldDiffMove(world2, worldDiff, WorldDiffMove.Cause.Other);
        this.assertDoMoveIsOk(m);
        this.assertUndoMoveIsOk(m);
        this.assertDoThenUndoLeavesWorldUnchanged(m);
        this.assertSurvivesSerialisation(m);
    }
}