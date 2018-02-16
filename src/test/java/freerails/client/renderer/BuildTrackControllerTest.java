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
import freerails.util.Vector2D;
import freerails.model.world.SharedKey;
import freerails.model.world.World;
import freerails.model.player.FreerailsPrincipal;
import freerails.model.terrain.FullTerrainTile;
import freerails.model.track.TrackRule;
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
        FreerailsPrincipal principal = world.getPlayer(0).getPrincipal();
        modelRoot.setup(world, principal);
        buildTrackController = new BuildTrackController(world, modelRoot);
        MoveExecutor executor = new SimpleMoveExecutor(world, 0);
        trackBuilder = new TrackMoveProducer(executor, world, modelRoot);

        for (int i = 0; i < world.size(SharedKey.TrackRules); i++) {

            final Integer ruleID = i;
            TrackRule rule = (TrackRule) world.get(SharedKey.TrackRules, i);

            if (rule.getTypeName().equals("standard track")) {
                singleTrackRuleID = ruleID;
            }
            if (rule.getTypeName().equals("double track")) {
                doubleTrackRuleID = ruleID;
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
        Vector2D from = new Vector2D(10, 10);
        modelRoot.setProperty(ModelRootProperty.CURSOR_POSITION, from);
        Vector2D to = new Vector2D(20, 10);
        buildTrackController.setProposedTrack(to, trackBuilder);
        buildTrackController.updateUntilComplete();
        assertTrue(buildTrackController.isBuildTrackSuccessful());

        // See if any track has actually been built.
        FullTerrainTile tile = (FullTerrainTile) world.getTile(new Vector2D(10, 10));
        assertFalse(tile.hasTrack());
        buildTrackController.updateWorld(trackBuilder);
        tile = (FullTerrainTile) world.getTile(new Vector2D(10, 10));
        assertTrue(tile.hasTrack());
        tile = (FullTerrainTile) world.getTile(new Vector2D(20, 10));
        assertTrue(tile.hasTrack());
    }

    /**
     *
     */
    public void testUpgradeTrack() {
        // Build the track.
        testBuildTrack();

        // Change the strategy.
        BuildTrackStrategy bts = BuildTrackStrategy.getSingleRuleInstance(doubleTrackRuleID, modelRoot.getWorld());

        trackBuilder.setBuildTrackStrategy(bts);
        trackBuilder.setTrackBuilderMode(BuildMode.UPGRADE_TRACK);

        // Upgrade part of the track.
        modelRoot.setProperty(ModelRootProperty.CURSOR_POSITION, new Vector2D(15, 10));
        buildTrackController.setProposedTrack(new Vector2D(20, 10), trackBuilder);
        buildTrackController.updateUntilComplete();
        assertTrue(buildTrackController.isBuildTrackSuccessful());

        buildTrackController.updateWorld(trackBuilder);

        FullTerrainTile tile = (FullTerrainTile) world.getTile(new Vector2D(10, 10));
        assertEquals(singleTrackRuleID, tile.getTrackPiece().getTrackTypeID());

        tile = (FullTerrainTile) world.getTile(new Vector2D(15, 10));
        assertEquals(doubleTrackRuleID, tile.getTrackPiece().getTrackTypeID());

        tile = (FullTerrainTile) world.getTile(new Vector2D(17, 10));
        assertEquals(doubleTrackRuleID, tile.getTrackPiece().getTrackTypeID());

        tile = (FullTerrainTile) world.getTile(new Vector2D(20, 10));
        assertEquals(doubleTrackRuleID, tile.getTrackPiece().getTrackTypeID());
    }

    /**
     *
     */
    public void testRemoveTrack() {
        // Build the track.
        testBuildTrack();

        // Then remove some of it.
        trackBuilder.setTrackBuilderMode(BuildMode.REMOVE_TRACK);
        Vector2D from = new Vector2D(15, 10);
        modelRoot.setProperty(ModelRootProperty.CURSOR_POSITION, from);

        Vector2D to = new Vector2D(20, 10);
        buildTrackController.setProposedTrack(to, trackBuilder);

        buildTrackController.updateUntilComplete();
        assertTrue(buildTrackController.isBuildTrackSuccessful());

        buildTrackController.updateWorld(trackBuilder);
    }

}
