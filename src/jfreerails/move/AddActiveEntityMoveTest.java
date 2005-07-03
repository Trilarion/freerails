/*
 * Created on 02-Jul-2005
 *
 */
package jfreerails.move;

import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.AKEY;
import jfreerails.world.top.Activity;
import jfreerails.world.top.WorldImplTest;


public class AddActiveEntityMoveTest extends AbstractMoveTestCase {

	@Override
	public void testMove() {
		FreerailsPrincipal p = getPrincipal();		
		Activity a = new WorldImplTest.TestActivity(50);
		AddActiveEntityMove move = new AddActiveEntityMove(a, 0, AKEY.TRAIN_POSITIONS, p);
		assertEqualsSurvivesSerialisation(move);				
		assertOkButNotRepeatable(move);
		AddActiveEntityMove move2 = new AddActiveEntityMove(a, 2, AKEY.TRAIN_POSITIONS, p);
		assertTryMoveFails(move2);
	}

}
