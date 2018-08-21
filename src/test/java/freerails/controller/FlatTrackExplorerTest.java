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

package freerails.controller;

import freerails.io.GsonManager;
import freerails.model.player.Player;
import freerails.model.terrain.Terrain;
import freerails.model.track.TrackType;
import freerails.model.track.explorer.FlatTrackExplorer;
import freerails.move.mapupdatemove.ChangeTrackPieceCompositeMove;
import freerails.nove.Status;
import freerails.util.Vec2D;
import freerails.model.game.Rules;
import freerails.model.terrain.TileTransition;
import freerails.model.track.NoTrackException;
import freerails.model.train.PositionOnTrack;
import freerails.model.world.World;
import freerails.util.WorldGenerator;
import junit.framework.TestCase;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.SortedSet;

/**
 * Test for FlatTrackExplorer.
 */
public class FlatTrackExplorerTest extends TestCase {

    private final Player testPlayer = WorldGenerator.TEST_PLAYER;
    private World world;

    /**
     *
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // load terrain types
        URL url = FlatTrackExplorerTest.class.getResource("/freerails/data/scenario/terrain_types.json");
        File file = new File(url.toURI());
        SortedSet<Terrain> terrainTypes = GsonManager.loadTerrainTypes(file);

        // load rules
        url = FlatTrackExplorerTest.class.getResource("/rules.without_restrictions.json");
        file = new File(url.toURI());
        Rules rules = GsonManager.load(file, Rules.class);

        // generate track types
        SortedSet<TrackType> trackTypes = WorldGenerator.testTrackTypes();

        world = new World.Builder().setMapSize(new Vec2D(20, 20)).setTerrainTypes(terrainTypes).setTrackTypes(trackTypes).setRules(rules).build();
        world.addPlayer(testPlayer);

        TrackType trackType = world.getTrackType(0);

        TileTransition[] vectors = {TileTransition.WEST, TileTransition.EAST, TileTransition.NORTH_EAST};
        Vec2D p = new Vec2D(10, 10);
        Vec2D[] points = {p, p, p};

        for (int i = 0; i < points.length; i++) {
            ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove
                    .generateBuildTrackMove(points[i], vectors[i], trackType, trackType, world, WorldGenerator.TEST_PLAYER);
            Status status = move.doMove(world, Player.AUTHORITATIVE);
            assertTrue(status.isSuccess());
        }
    }

    /**
     * @throws NoTrackException
     */
    public void testGetFirstVectorToTry() throws NoTrackException {
        PositionOnTrack p = new PositionOnTrack(new Vec2D(10, 10), TileTransition.SOUTH_WEST);
        FlatTrackExplorer fte = new FlatTrackExplorer(world, p);
        TileTransition v = fte.getFirstVectorToTry();
        assertEquals(TileTransition.EAST, v);
    }

    /**
     * Tests that the track explorer at point 10,10 tells us that we can move
     * west, east, or northeast.
     *
     * @throws NoTrackException if no track
     */
    public void testGetPossibleDirections() throws NoTrackException {
        FlatTrackExplorer fte;

        PositionOnTrack p = new PositionOnTrack(new Vec2D(10, 10), TileTransition.SOUTH_WEST);
        fte = new FlatTrackExplorer(world, p);

        // There should be 3 branches.
        assertTrue(fte.hasNextEdge());
        fte.nextEdge();
        p.setValuesFromInt(fte.getVertexConnectedByEdge());
        assertEquals(TileTransition.EAST, p.getComingFrom());
        assertTrue(fte.hasNextEdge());
        fte.nextEdge();

        p.setValuesFromInt(fte.getVertexConnectedByEdge());
        assertEquals(TileTransition.WEST, p.getComingFrom());

        assertTrue(fte.hasNextEdge());
        fte.nextEdge();
        p.setValuesFromInt(fte.getVertexConnectedByEdge());
        assertEquals(TileTransition.NORTH_EAST, p.getComingFrom());
        assertTrue(!fte.hasNextEdge());
    }

    /**
     * Tests that we can move the track explorer at point 10,10 northeast, and
     * that when we have done this, we can move it back again.
     *
     * @throws NoTrackException if no track
     */
    public void testMoveTrackExplorer() throws NoTrackException {
        FlatTrackExplorer fte;

        PositionOnTrack p = new PositionOnTrack(new Vec2D(10, 10), TileTransition.EAST);
        fte = new FlatTrackExplorer(world, p);

        PositionOnTrack pos = new PositionOnTrack(fte.getPosition());
        assertEquals(10, pos.getLocation().x);
        assertEquals(10, pos.getLocation().y);
        assertTrue(fte.hasNextEdge());
        fte.nextEdge();
        pos.setValuesFromInt(fte.getVertexConnectedByEdge());
        assertEquals(TileTransition.NORTH_EAST, pos.getComingFrom());
        assertEquals(11, pos.getLocation().x);
        assertEquals(9, pos.getLocation().y);

        int branchPosition = fte.getVertexConnectedByEdge();
        fte.moveForward();
        assertEquals(branchPosition, fte.getPosition());

        pos.setValuesFromInt(fte.getPosition());
        assertEquals(11, pos.getLocation().x);
        assertEquals(9, pos.getLocation().y);

        assertTrue(fte.hasNextEdge());
        fte.nextEdge();
        assertEquals(TileTransition.SOUTH_WEST, fte.currentBranch.getComingFrom());
        assertTrue(!fte.hasNextEdge());
        fte.moveForward();
        pos.setValuesFromInt(fte.getPosition());
        assertEquals(10, pos.getLocation().x);
        assertEquals(10, pos.getLocation().y);
    }

    /**
     * @throws NoTrackException
     */
    public void testHasNext() throws NoTrackException {
        FlatTrackExplorer explorer = new FlatTrackExplorer(world, new PositionOnTrack(new Vec2D(10, 10), TileTransition.EAST));
        assertTrue(explorer.hasNextEdge());
    }

    /**
     *
     */
    public void testNoTrack() {
        try {
            FlatTrackExplorer explorer = new FlatTrackExplorer(world,
                    new PositionOnTrack(new Vec2D(4, 7), TileTransition.EAST));
            fail("Expected an Exception");
        } catch (NoTrackException e) {
            // ignore
        }
    }

    /**
     *
     */
    public void testGetPossiblePositions() {
        PositionOnTrack[] positions = FlatTrackExplorer.getPossiblePositions(world, new Vec2D(10, 10));
        assertNotNull(positions);
        assertEquals(3, positions.length);

        HashSet<TileTransition> directions = new HashSet<>();
        directions.add(TileTransition.WEST);
        directions.add(TileTransition.EAST);
        directions.add(TileTransition.SOUTH_WEST);

        HashSet<TileTransition> directions2 = new HashSet<>();

        for (PositionOnTrack position : positions) {
            directions2.add(position.getComingFrom());
        }

        assertEquals(directions, directions2);
    }
}