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

/*
 *
 */
package freerails.client.renderer;

import freerails.client.ModelRootImpl;
import freerails.client.TestModelRoots;
import freerails.controller.*;
import freerails.client.ModelRootProperty;
import freerails.model.MapFixtureFactory2;
import freerails.model.track.BuildTrackStrategy;
import freerails.model.track.TrackType;
import freerails.move.MoveExecutor;
import freerails.move.SimpleMoveExecutor;
import freerails.util.IntPoint2D;
import freerails.model.world.World;
import freerails.model.player.Player;
import freerails.model.game.Rules;
import freerails.model.terrain.TerrainTile;
import freerails.model.terrain.TileTransition;
import freerails.model.track.TrackPiece;
import freerails.move.Status;
import junit.framework.TestCase;

import java.util.Map;

/**
 * Unit test for BuildTrackController.
 */

public class BuildTrackControllerTest extends TestCase {

    private World world;
    private ModelRootImpl modelRoot;
    private BuildTrackController buildTrackController;
    private TrackMoveProducer trackBuilder;
    private int singleTrackRuleID = -1;
    private int doubleTrackRuleID = -1;

    /**
     * Initializes the fixture before each test.
     * @throws Exception if the set up operation fails.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        world = MapFixtureFactory2.getCopy();
        Player player = world.getPlayer(0);
        modelRoot = TestModelRoots.create(world, player);
        buildTrackController = new BuildTrackController(world, modelRoot);
        MoveExecutor executor = new SimpleMoveExecutor(world, player);
        trackBuilder = new TrackMoveProducer(executor, modelRoot.getProperties());

        for (TrackType trackType: world.getTrackTypes()) {
            if (trackType.getName().equals("standard track")) {
                singleTrackRuleID = trackType.getId();
            }
            if (trackType.getName().equals("double track")) {
                doubleTrackRuleID = trackType.getId();
            }
        }
        assertFalse(singleTrackRuleID == -1);
        assertFalse(doubleTrackRuleID == -1);

        // unit tests should be silent!
        modelRoot.getProperties().setProperty(ModelRootProperty.PLAY_SOUNDS, false);
    }

    /**
     * Tests build track behavior.
     */
    public void testBuildTrack() {
        IntPoint2D from = new IntPoint2D(10, 10);
        modelRoot.getProperties().setProperty(ModelRootProperty.CURSOR_POSITION, from);
        IntPoint2D to = new IntPoint2D(20, 10);
        buildTrackController.setProposedTrack(to, trackBuilder);
        completeSearch();
        assertTrue(buildTrackController.isBuildTrackSuccessful());

        // See if any track has actually been built.
        TerrainTile tile = world.getTile(new IntPoint2D(10, 10));
        assertFalse(tile.hasTrack());
        buildTrackController.updateWorld(trackBuilder);
        tile = world.getTile(new IntPoint2D(10, 10));
        assertTrue(tile.hasTrack());
        tile = world.getTile(new IntPoint2D(20, 10));
        assertTrue(tile.hasTrack());
    }

    /**
     * Tests upgrade track behavior.
     */
    public void testUpgradeTrack() {
        // Build the track.
        testBuildTrack();

        // Change the strategy.
        BuildTrackStrategy buildTrackStrategy = BuildTrackStrategy.getSingleRuleInstance(doubleTrackRuleID, modelRoot.getWorld());

        trackBuilder.setBuildTrackStrategy(buildTrackStrategy);
        trackBuilder.setTrackBuilderMode(BuildMode.UPGRADE_TRACK);

        // Upgrade part of the track.
        modelRoot.getProperties().setProperty(ModelRootProperty.CURSOR_POSITION, new IntPoint2D(15, 10));
        buildTrackController.setProposedTrack(new IntPoint2D(20, 10), trackBuilder);
        completeSearch();
        assertTrue(buildTrackController.isBuildTrackSuccessful());

        buildTrackController.updateWorld(trackBuilder);

        TerrainTile tile = world.getTile(new IntPoint2D(10, 10));
        assertEquals(singleTrackRuleID, tile.trackPiece().trackType().getId());

        tile = world.getTile(new IntPoint2D(15, 10));
        assertEquals(doubleTrackRuleID, tile.trackPiece().trackType().getId());

        tile = world.getTile(new IntPoint2D(17, 10));
        assertEquals(doubleTrackRuleID, tile.trackPiece().trackType().getId());

        tile = world.getTile(new IntPoint2D(20, 10));
        assertEquals(doubleTrackRuleID, tile.trackPiece().trackType().getId());
    }

    /**
     * Tests remove track behavior.
     */
    public void testRemoveTrack() {
        // Build the track.
        testBuildTrack();

        // Then remove some of it.
        trackBuilder.setTrackBuilderMode(BuildMode.REMOVE_TRACK);
        IntPoint2D from = new IntPoint2D(15, 10);
        modelRoot.getProperties().setProperty(ModelRootProperty.CURSOR_POSITION, from);

        IntPoint2D to = new IntPoint2D(20, 10);
        buildTrackController.setProposedTrack(to, trackBuilder);

        completeSearch();
        assertTrue(buildTrackController.isBuildTrackSuccessful());

        buildTrackController.updateWorld(trackBuilder);
    }

    /**
     * Tests that a preview leaves the live world untouched and that each step
     * builds on the pieces proposed by the steps before it.
     */
    public void testPreviewBuildsOnEarlierStepsWithoutChangingTheWorld() {
        modelRoot.getProperties().setProperty(ModelRootProperty.CURSOR_POSITION, new IntPoint2D(10, 10));
        buildTrackController.setProposedTrack(new IntPoint2D(20, 10), trackBuilder);
        completeSearch();
        assertTrue(buildTrackController.isBuildTrackSuccessful());

        Map<?, ?> proposed = (Map<?, ?>) modelRoot.getProperties().getProperty(ModelRootProperty.PROPOSED_TRACK);
        for (int x = 10; x <= 20; x++) {
            IntPoint2D location = new IntPoint2D(x, 10);
            assertFalse(world.getTile(location).hasTrack());
            assertTrue(proposed.containsKey(location));
        }
        // A middle tile carries the connections of both sections it belongs to.
        TrackPiece middle = (TrackPiece) proposed.get(new IntPoint2D(15, 10));
        assertTrue(middle.trackConfiguration().contains(TileTransition.EAST));
        assertTrue(middle.trackConfiguration().contains(TileTransition.WEST));
    }

    /**
     * Tests that a preview of track that does not connect to the player's track is
     * refused when new track must stay connected.
     */
    public void testDisconnectedPreviewIsRefused() {
        world = MapFixtureFactory2.getCopy(new Rules(false, true));
        Player player = world.getPlayer(0);
        modelRoot = TestModelRoots.create(world, player);
        modelRoot.getProperties().setProperty(ModelRootProperty.PLAY_SOUNDS, false);
        buildTrackController = new BuildTrackController(world, modelRoot);
        trackBuilder = new TrackMoveProducer(new SimpleMoveExecutor(world, player), modelRoot.getProperties());
        assertTrue(trackBuilder.buildTrack(new IntPoint2D(10, 10), TileTransition.EAST).isSuccess());

        modelRoot.getProperties().setProperty(ModelRootProperty.CURSOR_POSITION, new IntPoint2D(30, 30));
        buildTrackController.setProposedTrack(new IntPoint2D(33, 30), trackBuilder);
        // The refused step abandons the search, which must then be over.
        completeSearch();

        assertFalse(buildTrackController.isBuildTrackSuccessful());
        assertEquals("Must connect to existing track", modelRoot.getProperties().getProperty(ModelRootProperty.CURSOR_MESSAGE));
    }

    /**
     * Tests that the search is over when no path exists or no search was started.
     */
    public void testSearchEndsWithoutAPath() {
        completeSearch();

        // There is no track to follow, so a removal preview finds no path.
        trackBuilder.setTrackBuilderMode(BuildMode.REMOVE_TRACK);
        modelRoot.getProperties().setProperty(ModelRootProperty.CURSOR_POSITION, new IntPoint2D(10, 10));
        buildTrackController.setProposedTrack(new IntPoint2D(20, 10), trackBuilder);
        completeSearch();

        assertFalse(buildTrackController.isBuildTrackSuccessful());
    }

    /**
     * Tests that a removal preview across a station proposes the removal of both
     * tiles of each section, and leaves the station in the live world.
     */
    public void testRemovalPreviewAcrossAStation() {
        testBuildTrack();
        StationBuilder stationBuilder = new StationBuilder(new SimpleMoveExecutor(world, world.getPlayer(0)));
        stationBuilder.setStationType(stationBuilder.getTrackTypeID("station"));
        IntPoint2D stationLocation = new IntPoint2D(15, 10);
        Status status = stationBuilder.buildStation(stationLocation);
        assertTrue(status.getMessage(), status.isSuccess());
        int stations = world.getStations(world.getPlayer(0)).size();

        trackBuilder.setTrackBuilderMode(BuildMode.REMOVE_TRACK);
        modelRoot.getProperties().setProperty(ModelRootProperty.CURSOR_POSITION, new IntPoint2D(14, 10));
        buildTrackController.setProposedTrack(new IntPoint2D(16, 10), trackBuilder);
        completeSearch();
        assertTrue(buildTrackController.isBuildTrackSuccessful());

        Map<?, ?> proposed = (Map<?, ?>) modelRoot.getProperties().getProperty(ModelRootProperty.PROPOSED_TRACK);
        assertTrue(proposed.containsKey(stationLocation));
        assertNull(proposed.get(stationLocation));
        assertTrue(proposed.containsKey(new IntPoint2D(16, 10)));
        assertTrue(world.getTile(stationLocation).hasTrack());
        assertEquals(stations, world.getStations(world.getPlayer(0)).size());
    }

    /**
     * Continues the path search the way the client does, by calling update, until
     * it is over.
     */
    private void completeSearch() {
        while (buildTrackController.isSearching()) {
            buildTrackController.update();
        }
    }
}
