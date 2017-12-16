/*
 * Created on 04-Jul-2005
 *
 */
package freerails.move;

import java.awt.Point;

import freerails.server.MapFixtureFactory2;
import freerails.world.player.Player;
import freerails.world.terrain.TerrainTile;

public class ChangeTileMoveTest extends AbstractMoveTestCase {

    @Override
    public void testMove() {
        Point p = new Point(10, 10);
        TerrainTile tile = (TerrainTile) world.getTile(10, 10);
        assertTrue(tile.getTerrainTypeID() != 5);
        ChangeTileMove move = new ChangeTileMove(world, p, 5);
        MoveStatus ms = move.doMove(world, Player.AUTHORITATIVE);
        assertTrue(ms.message, ms.ok);
        tile = (TerrainTile) world.getTile(10, 10);
        assertTrue(tile.getTerrainTypeID() == 5);
    }

    public void testMove2() {
        Point p = new Point(10, 10);
        ChangeTileMove move = new ChangeTileMove(world, p, 5);
        assertSurvivesSerialisation(move);

    }

    @Override
    protected void setupWorld() {
        world = MapFixtureFactory2.getCopy();
    }
}
