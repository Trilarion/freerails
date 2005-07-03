/*
 * Created on 02-Jul-2005
 *
 */
package jfreerails.move;

import jfreerails.world.common.GameTime;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.AKEY;
import jfreerails.world.top.Activity;
import jfreerails.world.top.ActivityIterator;
import jfreerails.world.top.World;
import jfreerails.world.top.WorldImplTest;



public class NextActivityMoveTest extends AbstractMoveTestCase {

	@Override
	public void testMove() {
		World w = getWorld();
		FreerailsPrincipal principal = getPrincipal();
		Activity act = new WorldImplTest.TestActivity(50);
		w.addActiveEntity(AKEY.TRAIN_POSITIONS, act, principal);
		
		Activity act2 = new WorldImplTest.TestActivity(60);
		Move move = new NextActivityMove(act2, 0, AKEY.TRAIN_POSITIONS, principal);
		assertEqualsSurvivesSerialisation(move);		
		assertOkAndRepeatable(move);
				
	}
	
	public void testMove2() {
		World w = getWorld();
		FreerailsPrincipal principal = getPrincipal();
		Activity act = new WorldImplTest.TestActivity(50);
		w.addActiveEntity(AKEY.TRAIN_POSITIONS, act, principal);
		
		Activity act2 = new WorldImplTest.TestActivity(60);
		Move move = new NextActivityMove(act2, 0, AKEY.TRAIN_POSITIONS, principal);
		assertDoThenUndoLeavesWorldUnchanged(move);
				
	}
	
	public void testStackingOfActivities(){
		World w = getWorld();
		FreerailsPrincipal principal = getPrincipal();
		Activity act = new WorldImplTest.TestActivity(50);
		w.addActiveEntity(AKEY.TRAIN_POSITIONS, act, principal);
		
		Activity act2 = new WorldImplTest.TestActivity(60);
		Move move = new NextActivityMove(act2, 0, AKEY.TRAIN_POSITIONS, principal);
		assertDoMoveIsOk(move);
		
		GameTime currentTime = new GameTime(0);
		assertEquals(currentTime, w.currentTime());
		ActivityIterator it = w.getActivities(AKEY.TRAIN_POSITIONS, 0, principal);
				
		assertEquals(it.getActivity(), act);
		assertEquals(it.getStartTime(), currentTime);
		assertEquals(50, it.getDuration());
		assertEquals(new GameTime(50), it.getFinishTime());
		
		assertTrue(it.hasNext());
		it.nextActivity();
		assertEquals(it.getActivity(), act2);
		assertEquals(new GameTime(50), it.getStartTime());
		assertEquals(60, it.getDuration());
		assertEquals( new GameTime(110), it.getFinishTime());
	}

}
