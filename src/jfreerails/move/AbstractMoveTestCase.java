/*
 * Copyright (C) 2003 Luke Lindsay
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

/*
 * Created on 28-Mar-2003
 *
 */
package jfreerails.move;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import jfreerails.world.accounts.BankAccount;
import jfreerails.world.common.GameCalendar;
import jfreerails.world.common.GameTime;
import jfreerails.world.player.Player;
import jfreerails.world.track.FreerailsTile;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.KEY;
import jfreerails.world.top.MapFixtureFactory;
import jfreerails.world.top.World;
import jfreerails.world.top.WorldImpl;
import junit.framework.TestCase;


/**
 * All TestCases for moves should extend this class.
 *
 * @author Luke
 *
 */
public abstract class AbstractMoveTestCase extends TestCase {
    private World world;
    private boolean hasSetupBeenCalled = false;

    protected Player testPlayer = new Player("test", (new
		Player("test")).getPublicKey(), 0);

    protected void setUp() {
        setHasSetupBeenCalled(true);
        setWorld(new WorldImpl(10, 10));
	for (int x = 0; x < 10; x++)
	    for (int y = 0; y < 10; y++)
		getWorld().setTile(x, y, new FreerailsTile(0));

	getWorld().add(KEY.PLAYERS, testPlayer, Player.AUTHORITATIVE);
        //		Set the time..
        getWorld().set(ITEM.CALENDAR, new GameCalendar(12000, 1840));
        getWorld().set(ITEM.TIME, new GameTime(0));
	getWorld().add(KEY.BANK_ACCOUNTS, new BankAccount(),
		testPlayer.getPrincipal());
        MapFixtureFactory.generateTrackRuleList(getWorld());
    }

    abstract public void testMove();

    protected void assertTryMoveIsOk(Move m) {
        assertSetupHasBeenCalled();

        MoveStatus ms = m.tryDoMove(getWorld(), testPlayer.getPrincipal());
        assertNotNull(ms);
        assertEquals("First try failed", MoveStatus.MOVE_OK, ms);

        ms = m.tryDoMove(getWorld(), testPlayer.getPrincipal());
        assertNotNull(ms);
        assertEquals("Second try failed, this suggests that the tryDoMove method failed to leave the world unchanged!",
            MoveStatus.MOVE_OK, ms);
    }

    protected void assertTryMoveFails(Move m) {
        assertSetupHasBeenCalled();

        MoveStatus ms = m.tryDoMove(getWorld(), testPlayer.getPrincipal());
        assertNotNull(ms);
        assertTrue("Move went through when it should have failed", !ms.ok);
    }

    protected void assertDoMoveIsOk(Move m) {
        assertSetupHasBeenCalled();

        MoveStatus ms = m.doMove(getWorld(), testPlayer.getPrincipal());
        assertNotNull(ms);
        assertEquals(MoveStatus.MOVE_OK, ms);
    }

    protected void assertDoMoveFails(Move m) {
        assertSetupHasBeenCalled();

        MoveStatus ms = m.doMove(getWorld(), testPlayer.getPrincipal());
        assertNotNull(ms);
        assertTrue("Move went through when it should have failed", !ms.ok);
    }

    protected void assertTryUndoMoveIsOk(Move m) {
        assertSetupHasBeenCalled();

        MoveStatus ms = m.tryUndoMove(getWorld(), testPlayer.getPrincipal());
        assertNotNull(ms);
        assertEquals("First try failed", MoveStatus.MOVE_OK, ms);

        ms = m.tryUndoMove(getWorld(), testPlayer.getPrincipal());
        assertNotNull(ms);
        assertEquals("Second try failed, this suggests that the tryDoMove method failed to leave the world unchanged!",
            MoveStatus.MOVE_OK, ms);
    }

    protected void assertTryUndoMoveFails(Move m) {
        assertSetupHasBeenCalled();

        MoveStatus ms = m.tryUndoMove(getWorld(), testPlayer.getPrincipal());
        assertNotNull(ms);
        assertTrue("Move went through when it should have failed", !ms.ok);
    }

    protected void assertUndoMoveIsOk(Move m) {
        assertSetupHasBeenCalled();

        MoveStatus ms = m.undoMove(getWorld(), testPlayer.getPrincipal());
        assertNotNull(ms);
        assertEquals(MoveStatus.MOVE_OK, ms);
    }

    protected void assertUndoMoveFails(Move m) {
        assertSetupHasBeenCalled();

        MoveStatus ms = m.tryUndoMove(getWorld(), testPlayer.getPrincipal());
        assertNotNull(ms);
        assertTrue("Move went through when it should have failed", !ms.ok);
    }

    /** Generally moves should not be repeatable.  For example,
     * if we have just removed a piece of track, that piece of
     * track is gone, so we cannot remove it again.
     */
    protected void assertOkButNotRepeatable(Move m) {
        assertSetupHasBeenCalled();

        assertTryMoveIsOk(m);
        assertDoMoveIsOk(m);
        assertTryMoveFails(m);
        assertDoMoveFails(m);
        assertTryUndoMoveIsOk(m);
        assertUndoMoveIsOk(m);
        assertTryUndoMoveFails(m);
        assertTryMoveIsOk(m);
        assertDoMoveIsOk(m);
    }

    /** This method asserts that if we serialise then deserialise the
     * specified move, the specified move is equal to the deserialised move.
     * The assertion depends on the move being serialisable and the equals method
     * being implemented correctly.
     *
     * @param m
     */
    protected void assertEqualsSurvivesSerialisation(Move m) {
        assertEquals("Reflexivity violated: the move does not equal itself", m,
            m);

        try {
            Object o = cloneBySerialisation(m);
            assertEquals(m, o);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    protected void assertDoThenUndoLeavesWorldUnchanged(Move m) {
        try {
            World copyOfWorldBefore = (World)cloneBySerialisation(getWorld());
            assertEquals("The world objects equals method did not survive serialization!",
                copyOfWorldBefore, getWorld());
            assertTrue(m.doMove(getWorld(), testPlayer.getPrincipal()).ok);
            assertTrue(m.undoMove(getWorld(), testPlayer.getPrincipal()).ok);
            assertEquals(copyOfWorldBefore, getWorld());
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public Object cloneBySerialisation(Object m)
        throws IOException, ClassNotFoundException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream objectOut = new ObjectOutputStream(out);
        objectOut.writeObject(m);
        objectOut.flush();

        byte[] bytes = out.toByteArray();

        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        ObjectInputStream objectIn = new ObjectInputStream(in);
        Object o = objectIn.readObject();

        return o;
    }

    protected void assertOkAndRepeatable(Move m) {
        assertSetupHasBeenCalled();

        //Do move
        assertTryMoveIsOk(m);
        assertDoMoveIsOk(m);
        assertTryMoveIsOk(m);
        assertDoMoveIsOk(m);

        //Since it leaves the world unchanged it should also be 
        //possible to undo it repeatably
        assertTryUndoMoveIsOk(m);
        assertUndoMoveIsOk(m);
        assertTryUndoMoveIsOk(m);
        assertUndoMoveIsOk(m);
    }

    private void assertSetupHasBeenCalled() {
        assertTrue("AbstractMoveTestCase.setUp has not been called!",
            hasSetupBeenCalled());
    }

    public AbstractMoveTestCase() {
    }

    public AbstractMoveTestCase(String str) {
        super(str);
    }

    protected void setHasSetupBeenCalled(boolean hasSetupBeenCalled) {
        this.hasSetupBeenCalled = hasSetupBeenCalled;
    }

    protected boolean hasSetupBeenCalled() {
        return hasSetupBeenCalled;
    }

    void setWorld(World world) {
        this.world = world;
    }

    World getWorld() {
        return world;
    }
}
