package jfreerails.move;

import jfreerails.world.top.World;
import jfreerails.world.top.WorldDifferences;
import jfreerails.world.track.FreerailsTile;


/**
 * JUnit test for MapDiffMove.
 * @author Luke
 */
public class MapDiffMoveTest extends AbstractMoveTestCase {
    public void testMove() {
        World world2 = this.getWorld();
        WorldDifferences worldDiff = new WorldDifferences(world2);

        FreerailsTile tile = (FreerailsTile)world2.getTile(2, 2);
        assertNotNull(tile);
        assertEquals(tile, worldDiff.getTile(2, 2));

        FreerailsTile newTile = FreerailsTile.getInstance(999);
        worldDiff.setTile(3, 5, newTile);
        assertEquals(newTile, worldDiff.getTile(3, 5));

        Move m = new MapDiffMove(world2, worldDiff);
        this.assertDoMoveIsOk(m);
        this.assertUndoMoveIsOk(m);
        this.assertDoThenUndoLeavesWorldUnchanged(m);
        this.assertEqualsSurvivesSerialisation(m);
    }
}