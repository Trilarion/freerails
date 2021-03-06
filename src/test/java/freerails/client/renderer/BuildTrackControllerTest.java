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
package freerails.client.renderer;

import freerails.client.ModelRootImpl;
import freerails.controller.*;
import freerails.client.ModelRootProperty;
import freerails.model.MapFixtureFactory2;
import freerails.model.track.BuildTrackStrategy;
import freerails.model.track.TrackType;
import freerails.move.MoveExecutor;
import freerails.move.SimpleMoveExecutor;
import freerails.util.Vec2D;
import freerails.model.world.World;
import freerails.model.player.Player;
import freerails.model.terrain.TerrainTile;
import junit.framework.TestCase;

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
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        world = MapFixtureFactory2.getCopy();
        modelRoot = new ModelRootImpl();
        Player player = world.getPlayer(0);
        modelRoot.setup(world, player);
        buildTrackController = new BuildTrackController(world, modelRoot);
        MoveExecutor executor = new SimpleMoveExecutor(world, player);
        trackBuilder = new TrackMoveProducer(executor, world, modelRoot);

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
        modelRoot.setProperty(ModelRootProperty.PLAY_SOUNDS, false);
    }

    /**
     *
     */
    public void testBuildTrack() {
        Vec2D from = new Vec2D(10, 10);
        modelRoot.setProperty(ModelRootProperty.CURSOR_POSITION, from);
        Vec2D to = new Vec2D(20, 10);
        buildTrackController.setProposedTrack(to, trackBuilder);
        buildTrackController.updateUntilComplete();
        assertTrue(buildTrackController.isBuildTrackSuccessful());

        // See if any track has actually been built.
        TerrainTile tile = world.getTile(new Vec2D(10, 10));
        assertFalse(tile.hasTrack());
        buildTrackController.updateWorld(trackBuilder);
        tile = world.getTile(new Vec2D(10, 10));
        assertTrue(tile.hasTrack());
        tile = world.getTile(new Vec2D(20, 10));
        assertTrue(tile.hasTrack());
    }

    /**
     *
     */
    public void testUpgradeTrack() {
        // Build the track.
        testBuildTrack();

        // Change the strategy.
        BuildTrackStrategy buildTrackStrategy = BuildTrackStrategy.getSingleRuleInstance(doubleTrackRuleID, modelRoot.getWorld());

        trackBuilder.setBuildTrackStrategy(buildTrackStrategy);
        trackBuilder.setTrackBuilderMode(BuildMode.UPGRADE_TRACK);

        // Upgrade part of the track.
        modelRoot.setProperty(ModelRootProperty.CURSOR_POSITION, new Vec2D(15, 10));
        buildTrackController.setProposedTrack(new Vec2D(20, 10), trackBuilder);
        buildTrackController.updateUntilComplete();
        assertTrue(buildTrackController.isBuildTrackSuccessful());

        buildTrackController.updateWorld(trackBuilder);

        TerrainTile tile = world.getTile(new Vec2D(10, 10));
        assertEquals(singleTrackRuleID, tile.getTrackPiece().getTrackType().getId());

        tile = world.getTile(new Vec2D(15, 10));
        assertEquals(doubleTrackRuleID, tile.getTrackPiece().getTrackType().getId());

        tile = world.getTile(new Vec2D(17, 10));
        assertEquals(doubleTrackRuleID, tile.getTrackPiece().getTrackType().getId());

        tile = world.getTile(new Vec2D(20, 10));
        assertEquals(doubleTrackRuleID, tile.getTrackPiece().getTrackType().getId());
    }

    /**
     *
     */
    public void testRemoveTrack() {
        // Build the track.
        testBuildTrack();

        // Then remove some of it.
        trackBuilder.setTrackBuilderMode(BuildMode.REMOVE_TRACK);
        Vec2D from = new Vec2D(15, 10);
        modelRoot.setProperty(ModelRootProperty.CURSOR_POSITION, from);

        Vec2D to = new Vec2D(20, 10);
        buildTrackController.setProposedTrack(to, trackBuilder);

        buildTrackController.updateUntilComplete();
        assertTrue(buildTrackController.isBuildTrackSuccessful());

        buildTrackController.updateWorld(trackBuilder);
    }

}
