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
 *
 */
package freerails.move;

import freerails.util.Point2D;
import freerails.util.Utils;
import freerails.world.*;
import freerails.world.game.GameCalendar;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.player.Player;
import freerails.world.terrain.FullTerrainTile;
import freerails.world.terrain.TileTransition;
import freerails.world.top.MapFixtureFactory;
import freerails.world.train.PathOnTiles;
import junit.framework.TestCase;

import java.io.Serializable;

/**
 * All TestCases for moves should extend this class.
 */
public abstract class AbstractMoveTestCase extends TestCase {

    /**
     *
     */
    protected World world;
    private boolean hasSetupBeenCalled = false;

    /**
     *
     */
    protected AbstractMoveTestCase() {
    }

    /**
     * @param str
     */
    public AbstractMoveTestCase(String str) {
        super(str);
    }

    /**
     * @param m
     */
    void assertDoMoveFails(Move m) {
        assertSetupHasBeenCalled();

        MoveStatus ms = m.doMove(world, Player.AUTHORITATIVE);
        assertNotNull(ms);
        assertTrue("Move went through when it should have failed", !ms.ok);
    }

    /**
     * @param m
     */
    protected void assertDoMoveIsOk(Move m) {
        assertSetupHasBeenCalled();

        MoveStatus ms = m.doMove(world, Player.AUTHORITATIVE);
        assertNotNull(ms);
        assertEquals(MoveStatus.MOVE_OK, ms);
    }

    /**
     * @param m
     */
    void assertDoThenUndoLeavesWorldUnchanged(Move m) {
        try {
            World w = world;
            World before = (World) Utils.cloneBySerialisation(w);

            assertEquals(before, w);

            assertTrue(m.doMove(w, Player.AUTHORITATIVE).ok);
            World after = (World) Utils.cloneBySerialisation(w);
            assertFalse(after.equals(before));

            boolean b = m.undoMove(w, Player.AUTHORITATIVE).ok;
            assertTrue(b);
            assertEquals(before, w);
        } catch (Exception e) {
            assertTrue(false);
        }
    }

    /**
     * This method asserts that if we serialise then deserialize the specified
     * move, the specified move is equal to the deserialized move. The assertion
     * depends on the move being serializable and the equals method being
     * implemented correctly. Also checks that the hashcode does not change.
     *
     * @param m m
     */
    protected void assertSurvivesSerialisation(Serializable m) {
        assertEquals("Reflexivity violated: the move does not equal itself", m,
                m);

        try {
            Object o = Utils.cloneBySerialisation(m);
            assertEquals(m, o);
            assertEquals("The hashcodes should be the same!", m.hashCode(), o
                    .hashCode());
        } catch (Exception e) {
            assertTrue(false);
        }
    }

    /**
     * @param m
     */
    void assertOkAndRepeatable(Move m) {
        assertSetupHasBeenCalled();

        // Do move
        assertTryMoveIsOk(m);
        assertDoMoveIsOk(m);
        assertTryMoveIsOk(m);
        assertDoMoveIsOk(m);

        // Since it leaves the world unchanged it should also be
        // possible to undo it repeatably
        assertTryUndoMoveIsOk(m);
        assertUndoMoveIsOk(m);
        assertTryUndoMoveIsOk(m);
        assertUndoMoveIsOk(m);
    }

    /**
     * Generally moves should not be repeatable. For example, if we have just
     * removed a piece of track, that piece of track is gone, so we cannot
     * remove it again.
     *
     * @param m
     */
    void assertOkButNotRepeatable(Move m) {
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

    private void assertSetupHasBeenCalled() {
        assertTrue("AbstractMoveTestCase.setUp has not been called!",
                hasSetupBeenCalled());
    }

    /**
     * @param m
     */
    void assertTryMoveFails(Move m) {
        assertSetupHasBeenCalled();

        MoveStatus ms = m.tryDoMove(world, Player.AUTHORITATIVE);
        assertNotNull(ms);
        assertTrue("Move went through when it should have failed", !ms.ok);
    }

    /**
     * @param m
     */
    void assertTryMoveIsOk(Move m) {
        assertSetupHasBeenCalled();

        MoveStatus ms = m.tryDoMove(world, Player.AUTHORITATIVE);
        assertNotNull(ms);
        assertEquals("First try failed", MoveStatus.MOVE_OK, ms);

        ms = m.tryDoMove(world, Player.AUTHORITATIVE);
        assertNotNull(ms);
        assertEquals(
                "Second try failed, this suggests that the tryDoMove method failed to leave the world unchanged!",
                MoveStatus.MOVE_OK, ms);
    }

    /**
     * @param m
     */
    void assertTryUndoMoveFails(Move m) {
        assertSetupHasBeenCalled();

        MoveStatus ms = m.tryUndoMove(world, Player.AUTHORITATIVE);
        assertNotNull(ms);
        assertTrue("Move went through when it should have failed", !ms.ok);
    }

    /**
     * @param m
     */
    void assertTryUndoMoveIsOk(Move m) {
        assertSetupHasBeenCalled();

        MoveStatus ms = m.tryUndoMove(world, Player.AUTHORITATIVE);
        assertNotNull(ms);
        assertEquals("First try failed", MoveStatus.MOVE_OK, ms);

        ms = m.tryUndoMove(world, Player.AUTHORITATIVE);
        assertNotNull(ms);
        assertEquals(
                "Second try failed, this suggests that the tryDoMove method failed to leave the world unchanged!",
                MoveStatus.MOVE_OK, ms);
    }

    /**
     * @param m
     */
    protected void assertUndoMoveIsOk(Move m) {
        assertSetupHasBeenCalled();

        MoveStatus ms = m.undoMove(world, Player.AUTHORITATIVE);
        assertNotNull(ms);
        assertEquals(MoveStatus.MOVE_OK, ms);
    }

    FreerailsPrincipal getPrincipal() {
        return world.getPlayer(0).getPrincipal();
    }

    World getWorld() {
        return world;
    }

    void setWorld(World world) {
        this.world = world;
    }

    /**
     * @return
     */
    private boolean hasSetupBeenCalled() {
        return hasSetupBeenCalled;
    }

    /**
     * @param hasSetupBeenCalled
     */
    void setHasSetupBeenCalled(boolean hasSetupBeenCalled) {
        this.hasSetupBeenCalled = hasSetupBeenCalled;
    }

    /**
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        this.hasSetupBeenCalled = true;
        setupWorld();
    }

    /**
     *
     */
    protected void setupWorld() {
        this.world = new WorldImpl(10, 10);
        // Set the time..
        world.set(ITEM.CALENDAR, new GameCalendar(12000, 1840));
        world.addPlayer(MapFixtureFactory.TEST_PLAYER);
    }

    /**
     *
     */
    public void testMove() {
    }

    /**
     * @param x
     * @param y
     */
    protected void assertTrackHere(int x, int y) {

        FullTerrainTile tile = (FullTerrainTile) world.getTile(x, y);
        assertTrue(tile.hasTrack());
    }

    /**
     * @param path
     */
    protected void assertTrackHere(PathOnTiles path) {
        Point2D start = path.getStart();
        int x = start.x;
        int y = start.y;
        for (int i = 0; i < path.steps(); i++) {
            assertTrackHere(x, y);
            TileTransition tileTransition = path.getStep(i);
            x += tileTransition.deltaX;
            y += tileTransition.deltaY;
            assertTrackHere(x, y);
        }
    }
}