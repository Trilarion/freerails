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
package freerails.controller;

import freerails.controller.explorer.BuildTrackExplorer;
import freerails.model.world.WorldItem;
import freerails.model.world.WorldSharedKey;
import freerails.move.mapupdatemove.ChangeTrackPieceCompositeMove;
import freerails.move.MoveStatus;
import freerails.util.Vector2D;
import freerails.model.*;
import freerails.model.game.GameRules;
import freerails.model.player.FreerailsPrincipal;
import freerails.model.player.Player;
import freerails.model.terrain.FullTerrainTile;
import freerails.model.terrain.TerrainCategory;
import freerails.model.terrain.TileTransition;
import freerails.model.terrain.TerrainTypeImpl;
import freerails.model.track.TrackRule;
import freerails.model.train.PositionOnTrack;
import freerails.model.world.FullWorld;
import junit.framework.TestCase;

/**
 * Test for BuildTrackExplorer.
 */
public class BuildTrackExplorerTest extends TestCase {

    private final Player testPlayer = new Player("test", 0);
    private FullWorld world;
    private FreerailsPrincipal principal;

    /**
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        world = new FullWorld(20, 20);
        world.addPlayer(testPlayer);
        world.set(WorldItem.GameRules, GameRules.NO_RESTRICTIONS);
        principal = testPlayer.getPrincipal();
        MapFixtureFactory.generateTrackRuleList(world);
    }

    /**
     * On a blank map, we should be able to build track in any direction as long
     * as it does not go off the map.
     */
    public void test1() {
        PositionOnTrack start;

        // Test starting in the middle of the map.
        start = PositionOnTrack.createComingFrom(new Vector2D(10, 10), TileTransition.NORTH);

        BuildTrackExplorer explorer = new BuildTrackExplorer(world, principal);
        explorer.setPosition(start.toInt());
        assertNextVertexIs(TileTransition.NORTH, 10, 9, explorer);
        assertNextVertexIs(TileTransition.NORTH_EAST, 11, 9, explorer);
        assertNextVertexIs(TileTransition.EAST, 11, 10, explorer);
        // We miss out SW, S, and SE since we don't want to double back on
        // ourselves.
        assertNextVertexIs(TileTransition.WEST, 9, 10, explorer);
        assertNextVertexIs(TileTransition.NORTH_WEST, 9, 9, explorer);
        assertFalse(explorer.hasNextEdge());

        // Test starting in the top left of the map.
        start = PositionOnTrack.createComingFrom(Vector2D.ZERO, TileTransition.SOUTH_EAST);
        explorer.setPosition(start.toInt());
        assertNextVertexIs(TileTransition.EAST, 1, 0, explorer);
        assertNextVertexIs(TileTransition.SOUTH_EAST, 1, 1, explorer);
        assertNextVertexIs(TileTransition.SOUTH, 0, 1, explorer);
        assertFalse(explorer.hasNextEdge());

        // Test starting in the bottom right of the map.
        start = PositionOnTrack.createComingFrom(new Vector2D(19, 19), TileTransition.NORTH_WEST);
        explorer.setPosition(start.toInt());
        assertNextVertexIs(TileTransition.NORTH, 19, 18, explorer);
        assertNextVertexIs(TileTransition.WEST, 18, 19, explorer);
        assertNextVertexIs(TileTransition.NORTH_WEST, 18, 18, explorer);
        assertFalse(explorer.hasNextEdge());
    }

    /**
     * Test when we cannot build on some terrain types.
     */
    public void test2() {
        // Check the the Ocean type is where we think it is.
        int occeanTypeNumber = 4;
        TerrainTypeImpl ocean = (TerrainTypeImpl) world.get(WorldSharedKey.TerrainTypes,
                occeanTypeNumber);
        assertEquals(TerrainCategory.Ocean, ocean.getCategory());

        // Check that track cannot be built on ocean.
        for (int i = 0; i < world.size(WorldSharedKey.TrackRules); i++) {
            TrackRule rule = (TrackRule) world.get(WorldSharedKey.TrackRules, i);
            assertFalse(rule.canBuildOnThisTerrainType(ocean.getCategory()));
        }

        // Place some ocean.
        FullTerrainTile tile = FullTerrainTile.getInstance(occeanTypeNumber);
        world.setTile(new Vector2D(10, 9), tile);
        world.setTile(new Vector2D(11, 10), tile);

        PositionOnTrack start;

        // Test starting in the middle of the map.
        start = PositionOnTrack.createComingFrom(new Vector2D(10, 10), TileTransition.NORTH);

        BuildTrackExplorer explorer = new BuildTrackExplorer(world, principal);
        explorer.setPosition(start.toInt());
        assertNextVertexIs(TileTransition.NORTH_EAST, 11, 9, explorer);
        // We miss out SW, S, and SE since we don't want to double back on
        // ourselves.
        assertNextVertexIs(TileTransition.WEST, 9, 10, explorer);
        assertNextVertexIs(TileTransition.NORTH_WEST, 9, 9, explorer);
        assertFalse(explorer.hasNextEdge());
    }

    /**
     * Test for illegal track configurations.
     */
    public void test3() {
        // Build some track, from 10, 10 diagonally SE.
        int y = 10;
        int x = 10;

        for (int i = 0; i < 4; i++) {
            TileTransition v = TileTransition.SOUTH_EAST;
            buildTrack(x, y, v);
            x += v.deltaX;
            y += v.deltaY;
        }

        // If we enter 10, 10 from the south, we should be able to build track S
        // & SW.
        PositionOnTrack start = PositionOnTrack.createComingFrom(new Vector2D(10, 10), TileTransition.SOUTH);
        BuildTrackExplorer explorer = new BuildTrackExplorer(world, principal);
        explorer.setPosition(start.toInt());
        // SE is going along existing track
        assertNextVertexIs(TileTransition.SOUTH_EAST, 11, 11, explorer);
        // S is building new track.
        assertNextVertexIs(TileTransition.SOUTH, 10, 11, explorer);
        assertFalse(explorer.hasNextEdge());

        // If we enter 10, 11 from the north, we should be able to build track
        // N, E, W, & NW.
        start = PositionOnTrack.createComingFrom(new Vector2D(10, 11), TileTransition.NORTH);
        explorer.setPosition(start.toInt());
        assertNextVertexIs(TileTransition.NORTH, 10, 10, explorer);
        assertNextVertexIs(TileTransition.EAST, 11, 11, explorer);
        assertNextVertexIs(TileTransition.WEST, 9, 11, explorer);
        assertNextVertexIs(TileTransition.NORTH_WEST, 9, 10, explorer);
        assertFalse(explorer.hasNextEdge());

        // If we enter 10, 12 from the north, we also should be able to build
        // track N, E, W, & NW.
        start = PositionOnTrack.createComingFrom(new Vector2D(10, 12), TileTransition.NORTH);
        explorer.setPosition(start.toInt());
        assertNextVertexIs(TileTransition.NORTH, 10, 11, explorer);
        assertNextVertexIs(TileTransition.EAST, 11, 12, explorer);
        assertNextVertexIs(TileTransition.WEST, 9, 12, explorer);
        assertNextVertexIs(TileTransition.NORTH_WEST, 9, 11, explorer);
        assertFalse(explorer.hasNextEdge());
    }

    private void assertNextVertexIs(TileTransition oneTileMoveVector, int x, int y, BuildTrackExplorer explorer) {
        assertTrue(explorer.hasNextEdge());
        explorer.nextEdge();
        PositionOnTrack pos = new PositionOnTrack(explorer.getVertexConnectedByEdge());
        assertEquals(PositionOnTrack.createComingFrom(new Vector2D(x, y), oneTileMoveVector), pos);
    }

    private void buildTrack(int x, int y, TileTransition direction) {
        TrackRule rule = (TrackRule) world.get(WorldSharedKey.TrackRules, 0);
        ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove
                .generateBuildTrackMove(new Vector2D(x, y), direction, rule,
                        rule, world, MapFixtureFactory.TEST_PRINCIPAL);
        MoveStatus ms = move.doMove(world, Player.AUTHORITATIVE);
        assertTrue(ms.succeeds());
    }
}