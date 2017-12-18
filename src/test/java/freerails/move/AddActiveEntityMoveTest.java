/*
 * Created on 02-Jul-2005
 *
 */
package freerails.move;

import freerails.world.common.Activity;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.top.WorldImplTest;

/**
 *
 * @author jkeller1
 */
public class AddActiveEntityMoveTest extends AbstractMoveTestCase {

    /**
     *
     */
    @Override
    public void testMove() {
        FreerailsPrincipal p = getPrincipal();
        Activity a = new WorldImplTest.TestActivity(50);
        AddActiveEntityMove move = new AddActiveEntityMove(a, 0, p);
        assertSurvivesSerialisation(move);
        assertOkButNotRepeatable(move);
        AddActiveEntityMove move2 = new AddActiveEntityMove(a, 2, p);
        assertTryMoveFails(move2);
    }

}
