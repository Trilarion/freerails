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

import freerails.client.common.ModelRootImpl;
import freerails.controller.BuildTrackStrategy;
import freerails.controller.ModelRoot.Property;
import freerails.controller.MoveExecutor;
import freerails.controller.SimpleMoveExecutor;
import freerails.controller.TrackMoveProducer;
import freerails.controller.BuildMode;
import freerails.server.MapFixtureFactory2;
import freerails.util.Point2D;
import freerails.world.SKEY;
import freerails.world.World;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.terrain.FullTerrainTile;
import freerails.world.track.TrackRule;
import junit.framework.TestCase;

/**
 * Unit test for BuildTrackController.
 */

public class BuildTrackControllerTest extends TestCase {

    private World w;

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
        w = MapFixtureFactory2.getCopy();
        modelRoot = new ModelRootImpl();
        FreerailsPrincipal p = w.getPlayer(0).getPrincipal();
        modelRoot.setup(w, p);
        buildTrackController = new BuildTrackController(w, modelRoot);
        MoveExecutor executor = new SimpleMoveExecutor(w, 0);
        trackBuilder = new TrackMoveProducer(executor, w, modelRoot);

        for (int i = 0; i < w.size(SKEY.TRACK_RULES); i++) {

            final Integer ruleID = i;
            TrackRule rule = (TrackRule) w.get(SKEY.TRACK_RULES, i);

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
        modelRoot.setProperty(Property.PLAY_SOUNDS, false);
    }

    /**
     *
     */
    public void testBuildTrack() {
        Point2D from = new Point2D(10, 10);
        modelRoot.setProperty(Property.CURSOR_POSITION, from);
        Point2D to = new Point2D(20, 10);
        buildTrackController.setProposedTrack(to, trackBuilder);
        buildTrackController.updateUntilComplete();
        assertTrue(buildTrackController.isBuildTrackSuccessful());

        // See if any track has actually been built.
        FullTerrainTile tile = (FullTerrainTile) w.getTile(10, 10);
        assertFalse(tile.hasTrack());
        buildTrackController.updateWorld(trackBuilder);
        tile = (FullTerrainTile) w.getTile(10, 10);
        assertTrue(tile.hasTrack());
        tile = (FullTerrainTile) w.getTile(20, 10);
        assertTrue(tile.hasTrack());

    }

    /**
     *
     */
    public void testUpgradeTrack() {
        // Build the track.
        testBuildTrack();

        // Change the strategy.
        BuildTrackStrategy bts = BuildTrackStrategy.getSingleRuleInstance(
                doubleTrackRuleID, modelRoot.getWorld());

        trackBuilder.setBuildTrackStrategy(bts);
        trackBuilder.setTrackBuilderMode(BuildMode.UPGRADE_TRACK);

        // Upgrade part of the track.
        modelRoot.setProperty(Property.CURSOR_POSITION, new Point2D(15, 10));
        buildTrackController
                .setProposedTrack(new Point2D(20, 10), trackBuilder);
        buildTrackController.updateUntilComplete();

        assertTrue(buildTrackController.isBuildTrackSuccessful());

        buildTrackController.updateWorld(trackBuilder);

        FullTerrainTile tile = (FullTerrainTile) w.getTile(10, 10);

        assertEquals(singleTrackRuleID, tile.getTrackPiece().getTrackTypeID());

        tile = (FullTerrainTile) w.getTile(15, 10);

        assertEquals(doubleTrackRuleID, tile.getTrackPiece().getTrackTypeID());

        tile = (FullTerrainTile) w.getTile(17, 10);

        assertEquals(doubleTrackRuleID, tile.getTrackPiece().getTrackTypeID());

        tile = (FullTerrainTile) w.getTile(20, 10);

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
        Point2D from = new Point2D(15, 10);
        modelRoot.setProperty(Property.CURSOR_POSITION, from);

        Point2D to = new Point2D(20, 10);
        buildTrackController.setProposedTrack(to, trackBuilder);

        buildTrackController.updateUntilComplete();

        assertTrue(buildTrackController.isBuildTrackSuccessful());

        buildTrackController.updateWorld(trackBuilder);

    }

}
