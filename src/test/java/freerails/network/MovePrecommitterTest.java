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
 * Created on Sep 11, 2004
 *
 */
package freerails.network;

import freerails.controller.PreMove;
import freerails.controller.PreMoveStatus;
import freerails.controller.TimeTickPreMove;
import freerails.move.Move;
import freerails.move.MoveStatus;
import freerails.move.TimeTickMove;
import freerails.world.common.GameTime;
import freerails.world.player.Player;
import freerails.world.top.World;
import freerails.world.top.WorldImpl;
import junit.framework.TestCase;

/**
 */
public class MovePrecommitterTest extends TestCase {
    private World w;

    private MovePrecommitter committer;

    /**
     *
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        w = new WorldImpl(10, 10);
        committer = new MovePrecommitter(w);
    }

    /**
     * Test simple case of precommitting then fully committing moves.
     */
    public void test1() {
        GameTime oldtime = getTime();
        GameTime newTime = oldtime.nextTick();
        assertFalse(oldtime.equals(newTime));

        Move m = new TimeTickMove(oldtime, newTime);
        MoveStatus ms = m.tryDoMove(w, Player.AUTHORITATIVE);
        assertTrue(ms.ok);

        committer.toServer(m);

        /* The move m should now have been precommitted. */
        assertEquals(newTime, getTime());
        assertFalse(committer.blocked);
        assertEquals(0, committer.uncomitted.size());
        assertEquals(1, committer.precomitted.size());

        committer.fromServer(ms);

        /* The move m should now be full committed. */
        assertFalse(committer.blocked);
        assertEquals(0, committer.uncomitted.size());
        assertEquals(0, committer.precomitted.size());
        assertEquals(newTime, getTime());
    }

    /**
     * Test test clash.
     */
    public void test2() {
        GameTime oldtime = getTime();
        GameTime newTime = oldtime.nextTick();
        assertFalse(oldtime.equals(newTime));

        Move m = new TimeTickMove(oldtime, newTime);
        MoveStatus ms = m.tryDoMove(w, Player.AUTHORITATIVE);
        assertTrue(ms.ok);

        committer.toServer(m);

        /* The move m should now have been precommitted. */
        assertEquals(newTime, getTime());
        assertFalse(committer.blocked);
        assertEquals(0, committer.uncomitted.size());
        assertEquals(1, committer.precomitted.size());

        committer.fromServer(m);
        assertFalse(m.tryDoMove(w, Player.AUTHORITATIVE).ok);

        /* The move m should now be full committed. */
        assertEquals(1, committer.uncomitted.size());
        assertEquals(0, committer.precomitted.size());

        assertEquals(newTime, getTime());

        committer.precommitMoves();

        /*
         * The committer should be block since the move on the uncommitted list
         * fails to go through.
         */
        assertTrue(committer.blocked);
        assertEquals(1, committer.uncomitted.size());
        assertEquals(0, committer.precomitted.size());

        Move m2 = new TimeTickMove(newTime, oldtime);
        committer.fromServer(m2);
        assertEquals(oldtime, getTime());
        committer.fromServer(ms);
        assertEquals(newTime, getTime());
        assertFalse(committer.blocked);
        assertEquals(0, committer.uncomitted.size());
        assertEquals(0, committer.precomitted.size());
    }

    /**
     * Test test rejection 1.
     */
    public void test3() {
        GameTime oldtime = getTime();
        GameTime newTime = oldtime.nextTick();
        assertFalse(oldtime.equals(newTime));

        Move m = new TimeTickMove(oldtime, newTime);
        MoveStatus ms = m.tryDoMove(w, Player.AUTHORITATIVE);
        assertTrue(ms.ok);

        committer.toServer(m);

        /* The move m should now have been precommitted. */
        assertEquals(newTime, getTime());
        assertFalse(committer.blocked);
        assertEquals(0, committer.uncomitted.size());
        assertEquals(1, committer.precomitted.size());

        /* Now, suppose the server rejected the move.. */
        MoveStatus rejection = MoveStatus.moveFailed("Rejected!");
        committer.fromServer(rejection);
        assertEquals(oldtime, getTime());
        assertFalse(committer.blocked);
        assertEquals(0, committer.uncomitted.size());
        assertEquals(0, committer.precomitted.size());
    }

    /**
     * Test test rejection 2.
     */
    public void test4() {
        GameTime oldtime = getTime();
        GameTime newTime = oldtime.nextTick();
        assertFalse(oldtime.equals(newTime));

        /* the following move should fail! */
        Move m = new TimeTickMove(newTime, oldtime);
        MoveStatus ms = m.tryDoMove(w, Player.AUTHORITATIVE);
        assertFalse(ms.ok);

        committer.toServer(m);
        assertTrue(committer.blocked);
        assertEquals(1, committer.uncomitted.size());
        assertEquals(0, committer.precomitted.size());

        committer.fromServer(ms);
        assertFalse(committer.blocked);
        assertEquals(0, committer.uncomitted.size());
        assertEquals(0, committer.precomitted.size());
    }

    /**
     *
     */
    public void testPreMoves1() {
        PreMove pm = TimeTickPreMove.INSTANCE;
        GameTime oldtime = getTime();
        GameTime newTime = oldtime.nextTick();
        committer.fromServer(pm);
        assertEquals(newTime, getTime());
    }

    /**
     *
     */
    public void testPreMoves2() {
        PreMove pm = TimeTickPreMove.INSTANCE;
        GameTime oldtime = getTime();
        GameTime newTime = oldtime.nextTick();

        // Send a premove to the server.
        committer.toServer(pm);
        assertEquals(0, committer.uncomitted.size());
        assertEquals(1, committer.precomitted.size());
        assertEquals(newTime, getTime());

        // The server accepts it..
        committer.fromServer(PreMoveStatus.PRE_MOVE_OK);
        assertEquals(0, committer.uncomitted.size());
        assertEquals(0, committer.precomitted.size());
        assertEquals(newTime, getTime());
    }

    /**
     *
     */
    public void testPreMoves3() {
        PreMove pm = TimeTickPreMove.INSTANCE;
        GameTime oldtime = getTime();
        GameTime newTime = oldtime.nextTick();

        // Send a premove to the server.
        committer.toServer(pm);
        assertEquals(0, committer.uncomitted.size());
        assertEquals(1, committer.precomitted.size());
        assertEquals(newTime, getTime());

        // The server rejects it.
        committer.fromServer(PreMoveStatus.failed("failed"));
        assertEquals(0, committer.uncomitted.size());
        assertEquals(0, committer.precomitted.size());
        assertEquals(oldtime, getTime());
    }

    private GameTime getTime() {
        return w.currentTime();
    }
}