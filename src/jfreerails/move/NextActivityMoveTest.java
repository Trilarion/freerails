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
		Move move = new NextActivityMove(act2, 0, AKEY.TRAIN_POSITIONS,
				principal);
		assertSurvivesSerialisation(move);
		assertOkAndRepeatable(move);

	}

	public void testMove2() {
		World w = getWorld();
		FreerailsPrincipal principal = getPrincipal();
		Activity act = new WorldImplTest.TestActivity(50);
		w.addActiveEntity(AKEY.TRAIN_POSITIONS, act, principal);

		Activity act2 = new WorldImplTest.TestActivity(60);
		Move move = new NextActivityMove(act2, 0, AKEY.TRAIN_POSITIONS,
				principal);
		assertDoThenUndoLeavesWorldUnchanged(move);

	}

	public void testStackingOfActivities() {
		World w = getWorld();
		FreerailsPrincipal principal = getPrincipal();
		Activity act = new WorldImplTest.TestActivity(50);
		w.addActiveEntity(AKEY.TRAIN_POSITIONS, act, principal);

		Activity act2 = new WorldImplTest.TestActivity(60);
		Move move = new NextActivityMove(act2, 0, AKEY.TRAIN_POSITIONS,
				principal);
		assertDoMoveIsOk(move);

		GameTime currentTime = new GameTime(0);
		assertEquals(currentTime, w.currentTime());
		ActivityIterator it = w.getActivities(AKEY.TRAIN_POSITIONS, 0,
				principal);

		assertEquals(it.getActivity(), act);
		assertEquals(it.getStartTime(), currentTime.getTicks(), 0.00001);
		assertEquals(50d, it.getDuration(), 0.00001);
		assertEquals(50d, it.getFinishTime(), 0.00001);

		assertTrue(it.hasNext());
		it.nextActivity();
		assertEquals(it.getActivity(), act2);
		assertEquals(50, it.getStartTime(), 0.00001);
		assertEquals(60, it.getDuration(), 0.0001d);
		assertEquals(110, it.getFinishTime(), 0.00001);
	}

}
