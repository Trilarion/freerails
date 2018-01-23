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

import freerails.server.MapFixtureFactory2;
import freerails.util.Point2D;
import freerails.world.player.Player;
import freerails.world.terrain.TerrainTile;

import java.awt.*;

/**
 *
 */
public class ChangeCityTileMoveTest extends AbstractMoveTestCase {

    /**
     *
     */
    @Override
    public void testMove() {
        Point2D p = new Point2D(10, 10);
        TerrainTile tile = (TerrainTile) world.getTile(p);
        assertTrue(tile.getTerrainTypeID() != 5);
        ChangeTileMove move = new ChangeTileMove(world, p, 5);
        MoveStatus moveStatus = move.doMove(world, Player.AUTHORITATIVE);
        assertTrue(moveStatus.getMessage(), moveStatus.succeeds());
        tile = (TerrainTile) world.getTile(p);
        assertTrue(tile.getTerrainTypeID() == 5);
    }

    /**
     *
     */
    public void testMove2() {
        Point2D p = new Point2D(10, 10);
        ChangeTileMove move = new ChangeTileMove(world, p, 5);
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
