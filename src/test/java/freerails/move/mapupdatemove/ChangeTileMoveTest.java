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
package freerails.move.mapupdatemove;

import freerails.model.terrain.TerrainTile;
import freerails.move.AbstractMoveTestCase;
import freerails.move.Status;
import freerails.model.MapFixtureFactory2;
import freerails.util.Vec2D;

/**
 *
 */
public class ChangeTileMoveTest extends AbstractMoveTestCase {

    /**
     *
     */
    public void testMove() {
        Vec2D p = new Vec2D(10, 10);
        TerrainTile tile = world.getTile(p);
        assertTrue(tile.getTerrainTypeId() != 5);

        ChangeTileMove move = new ChangeTileMove(new TerrainTile(5, world.getTile(p).getTrackPiece()), p);
        Status status = move.applicable(world);
        assertTrue(status.getMessage(), status.isSuccess());
        move.apply(world);
        tile = world.getTile(p);
        assertTrue(tile.getTerrainTypeId() == 5);
    }

    /**
     *
     */
    public void testMove2() {
        Vec2D p = new Vec2D(10, 10);
        ChangeTileMove move = new ChangeTileMove(new TerrainTile(5, world.getTile(p).getTrackPiece()), p);
        assertSurvivesSerialisation(move);
    }

    /**
     *
     */
    @Override
    protected void setupWorld() {
        world = MapFixtureFactory2.getCopy();
    }
}
