/*
 * Created on 02-Jul-2005
 *
 */
package freerails.move;

import freerails.world.common.Activity;
import freerails.world.common.ActivityIterator;
import freerails.world.common.GameTime;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.top.World;
import freerails.world.top.WorldImplTest;

/**
 *
 * @author jkeller1
 */
public class NextActivityMoveTest extends AbstractMoveTestCase {

    /**
     *
     */
    @Override
    public void testMove() {
        World w = getWorld();
        FreerailsPrincipal principal = getPrincipal();
        Activity act = new WorldImplTest.TestActivity(50);
        w.addActiveEntity(principal, act);

        Activity act2 = new WorldImplTest.TestActivity(60);
        Move move = new NextActivityMove(act2, 0, principal);
        assertSurvivesSerialisation(move);
        assertOkAndRepeatable(move);

    }

    /**
     *
     */
    public void testMove2() {
        World w = getWorld();
        FreerailsPrincipal principal = getPrincipal();
        Activity act = new WorldImplTest.TestActivity(50);
        w.addActiveEntity(principal, act);

        Activity act2 = new WorldImplTest.TestActivity(60);
        Move move = new NextActivityMove(act2, 0, principal);
        assertDoThenUndoLeavesWorldUnchanged(move);

    }

    /**
     *
     */
    public void testStackingOfActivities() {
        World w = getWorld();
        FreerailsPrincipal principal = getPrincipal();
        Activity act = new WorldImplTest.TestActivity(50);
        w.addActiveEntity(principal, act);

        Activity act2 = new WorldImplTest.TestActivity(60);
        Move move = new NextActivityMove(act2, 0, principal);
        assertDoMoveIsOk(move);

        GameTime currentTime = new GameTime(0);
        assertEquals(currentTime, w.currentTime());
        ActivityIterator it = w.getActivities(principal, 0);

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
