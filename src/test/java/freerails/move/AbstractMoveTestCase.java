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

import freerails.model.train.Train;
import freerails.util.Vec2D;
import freerails.util.Utils;
import freerails.model.player.Player;
import freerails.model.terrain.TerrainTile;
import freerails.model.terrain.TileTransition;
import freerails.model.train.PathOnTiles;
import freerails.model.world.World;
import freerails.util.WorldGenerator;
import junit.framework.TestCase;

import java.io.Serializable;

/**
 * All TestCases for moves should extend this class.
 */
public abstract class AbstractMoveTestCase extends TestCase {

    protected World world;
    protected int validEngineId;
    private boolean hasSetupBeenCalled = false;

    public AbstractMoveTestCase() {
    }

    /**
     * @param str
     */
    public AbstractMoveTestCase(String str) {
        super(str);
    }

    /**
     * @param move
     */
    void assertMoveApplyFails(Move move) {
        assertSetupHasBeenCalled();

        // TODO this doesn't really test if apply fails (rather surround with try and catch)
        Status status = move.applicable(world);
        assertTrue("Move went through when it should have failed", !status.isSuccess());
    }

    /**
     * @param move
     */
    protected void assertMoveApplyIsOk(Move move) {
        assertSetupHasBeenCalled();

        Status status = move.applicable(world);
        assertEquals(Status.OK, status);
        move.apply(world);
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
        assertEquals("Reflexivity violated: the move does not equal itself", m, m);

        try {
            Object o = Utils.cloneBySerialisation(m);
            assertEquals(m, o);
            assertEquals("The hashcodes should be the same!", m.hashCode(), o.hashCode());
        } catch (Exception e) {
            assertTrue(false);
        }
    }

    /**
     * @param move
     */
    void assertOkAndRepeatable(Move move) {
        assertSetupHasBeenCalled();

        // Do move
        assertMoveApplicable(move);
        assertMoveApplyIsOk(move);
        assertMoveApplicable(move);
        assertMoveApplyIsOk(move);
    }

    /**
     * Generally moves should not be repeatable. For example, if we have just
     * removed a piece of track, that piece of track is gone, so we cannot
     * remove it again.
     *
     * @param move
     */
    public void assertOkButNotRepeatable(Move move) {
        assertSetupHasBeenCalled();

        assertMoveApplicable(move);
        assertMoveApplyIsOk(move);
        assertMoveNotApplicable(move);
        assertMoveApplyFails(move);
    }

    private void assertSetupHasBeenCalled() {
        assertTrue("AbstractMoveTestCase.setUp has not been called!",
                hasSetupBeenCalled());
    }

    /**
     * @param move
     */
    public void assertMoveNotApplicable(Move move) {
        assertSetupHasBeenCalled();

        Status status = move.applicable(world);
        assertNotNull(status);
        assertTrue("Move went through when it should have failed", !status.isSuccess());
    }

    /**
     * @param move
     */
    public void assertMoveApplicable(Move move) {
        assertSetupHasBeenCalled();

        Status status = move.applicable(world);
        assertNotNull(status);
        assertEquals("First try failed", Status.OK, status);

        status = move.applicable(world);
        assertNotNull(status);
        assertEquals("Second try failed, this suggests that the tryDoMove method failed to leave the world unchanged!", Status.OK, status);
    }

    Player getPlayer() {
        return world.getPlayer(0);
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
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
    public void setHasSetupBeenCalled(boolean hasSetupBeenCalled) {
        this.hasSetupBeenCalled = hasSetupBeenCalled;
    }

    /**
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        hasSetupBeenCalled = true;
        setupWorld();
    }

    /**
     *
     */
    protected void setupWorld() {
        world = WorldGenerator.defaultWorld();

        validEngineId = world.getEngines().iterator().next().getId(); // more or less gets a valid id of an engine
        // Set the time..
        world.addPlayer(WorldGenerator.TEST_PLAYER);
        // add a train
        world.addTrain(WorldGenerator.TEST_PLAYER, new Train(0, 0));
    }

    /**
     * @param x
     * @param y
     */
    protected void assertTrackHere(int x, int y) {

        TerrainTile tile = world.getTile(new Vec2D(x, y));
        assertTrue(tile.hasTrack());
    }

    /**
     * @param path
     */
    protected void assertTrackHere(PathOnTiles path) {
        Vec2D start = path.getStart();
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