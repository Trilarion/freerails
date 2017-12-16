/*
 * Created on 02-Jul-2005
 *
 */
package freerails.move;

import freerails.util.Utils;
import freerails.world.player.Player;

public class AddPlayerMoveTest extends AbstractMoveTestCase {

    @Override
    public void testMove() {
        Player newPlayer = new Player("New Player");
        assertTrue("Check reflexivity of Player.equals(.)", Utils
                .equalsBySerialization(newPlayer, newPlayer));
        AddPlayerMove move = AddPlayerMove.generateMove(getWorld(), newPlayer);
        assertSurvivesSerialisation(move);
        assertDoThenUndoLeavesWorldUnchanged(move);

    }

    public void testMove2() {
        Player newPlayer = new Player("New Player");

        AddPlayerMove move = AddPlayerMove.generateMove(getWorld(), newPlayer);
        assertOkButNotRepeatable(move);

    }

}
