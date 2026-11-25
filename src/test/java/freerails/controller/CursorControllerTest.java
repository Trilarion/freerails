/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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

package freerails.controller;

import freerails.client.ModelRootImpl;
import freerails.client.ModelRootProperty;
import freerails.model.MapFixtureFactory2;
import freerails.model.player.Player;
import freerails.model.terrain.TerrainTile;
import freerails.model.terrain.TileTransition;
import freerails.model.track.TrackPiece;
import freerails.model.world.World;
import freerails.move.SimpleMoveExecutor;
import freerails.util.IntPoint2D;
import junit.framework.TestCase;

/**
 * Tests keyboard cursor moves: only number pad moves in build track mode build
 * track, and a cursor move never upgrades or removes track.
 */
public class CursorControllerTest extends TestCase {

    private static final IntPoint2D START = new IntPoint2D(10, 10);
    private static final IntPoint2D EAST = new IntPoint2D(11, 10);

    private World world;
    private ModelRootImpl modelRoot;
    private TrackMoveProducer producer;
    private CursorController cursorController;

    /**
     * Initializes the fixture before each test.
     * @throws Exception if the set up operation fails.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        world = MapFixtureFactory2.getCopy();
        Player player = world.getPlayer(0);
        modelRoot = new ModelRootImpl();
        modelRoot.setup(world, player);
        producer = new TrackMoveProducer(new SimpleMoveExecutor(world, player), world, modelRoot);
        cursorController = new CursorController(modelRoot);
        modelRoot.setProperty(ModelRootProperty.CURSOR_POSITION, START);
    }

    /**
     * Tests that a move that may build track builds it in build track mode.
     */
    public void testNumberPadMoveBuildsTrack() {
        producer.setTrackBuilderMode(BuildMode.BUILD_TRACK);

        assertTrue(cursorController.moveCursor(TileTransition.EAST, true, producer));

        assertEquals(EAST, modelRoot.getProperty(ModelRootProperty.CURSOR_POSITION));
        assertTrue(world.getTile(START).trackPiece().trackConfiguration().contains(TileTransition.EAST));
        assertTrue(world.getTile(EAST).trackPiece().trackConfiguration().contains(TileTransition.WEST));
        assertNull(modelRoot.getProperty(ModelRootProperty.CURSOR_MESSAGE));
    }

    /**
     * Tests that a move that may not build track only moves the cursor.
     */
    public void testControlArrowMoveOnlyMovesTheCursor() {
        producer.setTrackBuilderMode(BuildMode.BUILD_TRACK);

        assertTrue(cursorController.moveCursor(TileTransition.EAST, false, producer));

        assertEquals(EAST, modelRoot.getProperty(ModelRootProperty.CURSOR_POSITION));
        assertFalse(world.getTile(START).hasTrack());
        assertFalse(world.getTile(EAST).hasTrack());
    }

    /**
     * Tests that in the other modes a number pad move leaves track unchanged.
     */
    public void testOtherModesOnlyMoveTheCursor() {
        producer.setTrackBuilderMode(BuildMode.BUILD_TRACK);
        assertTrue(cursorController.moveCursor(TileTransition.EAST, true, producer));
        TrackPiece startTrack = world.getTile(START).trackPiece();
        TrackPiece eastTrack = world.getTile(EAST).trackPiece();

        for (BuildMode mode : new BuildMode[]{BuildMode.UPGRADE_TRACK, BuildMode.REMOVE_TRACK, BuildMode.INSPECT}) {
            producer.setTrackBuilderMode(mode);
            modelRoot.setProperty(ModelRootProperty.CURSOR_POSITION, START);

            assertTrue(cursorController.moveCursor(TileTransition.EAST, true, producer));

            assertEquals(mode.toString(), EAST, modelRoot.getProperty(ModelRootProperty.CURSOR_POSITION));
            assertEquals(mode.toString(), startTrack, world.getTile(START).trackPiece());
            assertEquals(mode.toString(), eastTrack, world.getTile(EAST).trackPiece());
        }
    }

    /**
     * Tests that a move off the map changes neither the cursor nor the track.
     */
    public void testMoveOffTheMapChangesNothing() {
        producer.setTrackBuilderMode(BuildMode.BUILD_TRACK);
        modelRoot.setProperty(ModelRootProperty.CURSOR_POSITION, IntPoint2D.ZERO);

        assertFalse(cursorController.moveCursor(TileTransition.NORTH, true, producer));

        assertEquals(IntPoint2D.ZERO, modelRoot.getProperty(ModelRootProperty.CURSOR_POSITION));
        assertFalse(world.getTile(IntPoint2D.ZERO).hasTrack());
    }

    /**
     * Tests that a failed build still moves the cursor and shows the reason.
     */
    public void testFailedBuildShowsTheReason() {
        producer.setTrackBuilderMode(BuildMode.BUILD_TRACK);
        // Track that is already there cannot be built again.
        assertTrue(cursorController.moveCursor(TileTransition.EAST, true, producer));
        modelRoot.setProperty(ModelRootProperty.CURSOR_POSITION, START);

        assertTrue(cursorController.moveCursor(TileTransition.EAST, true, producer));

        assertEquals(EAST, modelRoot.getProperty(ModelRootProperty.CURSOR_POSITION));
        assertNotNull(modelRoot.getProperty(ModelRootProperty.CURSOR_MESSAGE));
    }

    /**
     * Tests that the cursor moves when there is no track builder yet.
     */
    public void testMoveWithoutTrackBuilder() {
        assertTrue(cursorController.moveCursor(TileTransition.EAST, true, null));

        assertEquals(EAST, modelRoot.getProperty(ModelRootProperty.CURSOR_POSITION));
        assertFalse(world.getTile(START).hasTrack());
    }
}
