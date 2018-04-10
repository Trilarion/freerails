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

package freerails.move.mapupdatemove;

import freerails.move.AbstractMoveTestCase;
import freerails.move.Move;
import freerails.move.WorldDiffMoveCause;
import freerails.util.Vec2D;
import freerails.model.world.FullWorldDiffs;
import freerails.model.world.World;
import freerails.model.terrain.FullTerrainTile;

/**
 * Test for MapDiffMove.
 */
public class MapDiffMoveTest extends AbstractMoveTestCase {

    /**
     *
     */
    public void testMove() {
        World world2 = this.getWorld();
        FullWorldDiffs worldDiff = new FullWorldDiffs(world2);

        FullTerrainTile tile = (FullTerrainTile) world2.getTile(new Vec2D(2, 2));
        assertNotNull(tile);
        assertEquals(tile, worldDiff.getTile(new Vec2D(2, 2)));

        FullTerrainTile newTile = FullTerrainTile.getInstance(999);
        worldDiff.setTile(new Vec2D(3, 5), newTile);
        assertEquals(newTile, worldDiff.getTile(new Vec2D(3, 5)));

        Move move = new WorldDiffMove(world2, worldDiff, WorldDiffMoveCause.Other);
        this.assertDoMoveIsOk(move);
        this.assertUndoMoveIsOk(move);
        this.assertDoThenUndoLeavesWorldUnchanged(move);
        this.assertSurvivesSerialisation(move);
    }
}