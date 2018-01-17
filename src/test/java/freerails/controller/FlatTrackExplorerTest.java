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

import freerails.move.ChangeTrackPieceCompositeMove;
import freerails.move.MoveStatus;
import freerails.util.Point2D;
import freerails.world.*;
import freerails.world.game.GameRules;
import freerails.world.player.Player;
import freerails.world.terrain.TileTransition;
import freerails.world.top.MapFixtureFactory;
import freerails.world.track.TrackRule;
import freerails.world.train.PositionOnTrack;
import junit.framework.TestCase;

import java.util.HashSet;

/**
 * Test for FlatTrackExplorer.
 *
 * 24-Nov-2002
 */
public class FlatTrackExplorerTest extends TestCase {
    private final Player testPlayer = new Player("test", 0);
    private WorldImpl world;

    /**
     * @param arg0
     */
    public FlatTrackExplorerTest(String arg0) {
        super(arg0);
    }

    /**
     *
     */
    @Override
    protected void setUp() {
        world = new WorldImpl(20, 20);
        world.addPlayer(testPlayer);
        world.set(ITEM.GAME_RULES, GameRules.NO_RESTRICTIONS);
        MapFixtureFactory.generateTrackRuleList(world);

        TrackRule rule = (TrackRule) world.get(SKEY.TRACK_RULES, 0);

        TileTransition[] vectors = {TileTransition.WEST, TileTransition.EAST, TileTransition.NORTH_EAST};
        Point2D p = new Point2D(10, 10);
        Point2D[] points = {p, p, p};

        for (int i = 0; i < points.length; i++) {
            ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove
                    .generateBuildTrackMove(points[i], vectors[i], rule, rule,
                            world, MapFixtureFactory.TEST_PRINCIPAL);
            MoveStatus ms = move.doMove(world, Player.AUTHORITATIVE);
            assertTrue(ms.status);
        }
    }

    /**
     * @throws NoTrackException
     */
    public void testGetFirstVectorToTry() throws NoTrackException {
        setUp();

        PositionOnTrack p = PositionOnTrack.createComingFrom(10, 10,
                TileTransition.SOUTH_WEST);
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
        setUp();

        FlatTrackExplorer fte;

        PositionOnTrack p = PositionOnTrack.createComingFrom(10, 10,
                TileTransition.SOUTH_WEST);
        fte = new FlatTrackExplorer(world, p);

        // There should be 3 branches.
        assertTrue(fte.hasNextEdge());
        fte.nextEdge();
        p.setValuesFromInt(fte.getVertexConnectedByEdge());
        assertEquals(TileTransition.EAST, p.cameFrom());
        assertTrue(fte.hasNextEdge());
        fte.nextEdge();

        p.setValuesFromInt(fte.getVertexConnectedByEdge());
        assertEquals(TileTransition.WEST, p.cameFrom());

        assertTrue(fte.hasNextEdge());
        fte.nextEdge();
        p.setValuesFromInt(fte.getVertexConnectedByEdge());
        assertEquals(TileTransition.NORTH_EAST, p.cameFrom());
        assertTrue(!fte.hasNextEdge());
    }

    /**
     * Tests that we can move the track explorer at point 10,10 northeast, and
     * that when we have done this, we can move it back again.
     *
     * @throws NoTrackException if no track
     */
    public void testMoveTrackExplorer() throws NoTrackException {
        setUp();

        FlatTrackExplorer fte;

        PositionOnTrack p = PositionOnTrack.createComingFrom(10, 10, TileTransition.EAST);
        fte = new FlatTrackExplorer(world, p);

        PositionOnTrack pos = new PositionOnTrack(fte.getPosition());
        assertEquals(10, pos.getX());
        assertEquals(10, pos.getY());
        assertTrue(fte.hasNextEdge());
        fte.nextEdge();
        pos.setValuesFromInt(fte.getVertexConnectedByEdge());
        assertEquals(TileTransition.NORTH_EAST, pos.cameFrom());
        assertEquals(11, pos.getX());
        assertEquals(9, pos.getY());

        int branchPosition = fte.getVertexConnectedByEdge();
        fte.moveForward();
        assertEquals(branchPosition, fte.getPosition());

        pos.setValuesFromInt(fte.getPosition());
        assertEquals(11, pos.getX());
        assertEquals(9, pos.getY());

        assertTrue(fte.hasNextEdge());
        fte.nextEdge();
        assertEquals(TileTransition.SOUTH_WEST, fte.currentBranch.cameFrom());
        assertTrue(!fte.hasNextEdge());
        fte.moveForward();
        pos.setValuesFromInt(fte.getPosition());
        assertEquals(10, pos.getX());
        assertEquals(10, pos.getY());
    }

    /**
     * @throws NoTrackException
     */
    public void testHasNext() throws NoTrackException {
        setUp();

        FlatTrackExplorer explorer = new FlatTrackExplorer(world,
                PositionOnTrack.createComingFrom(10, 10, TileTransition.EAST));
        assertTrue(explorer.hasNextEdge());
    }

    /**
     *
     */
    public void testNoTrack() {
        setUp();

        try {
            FlatTrackExplorer explorer = new FlatTrackExplorer(world,
                    PositionOnTrack.createComingFrom(4, 7, TileTransition.EAST));
            fail("Expected an Exception");
        } catch (NoTrackException e) {
            // ignore
        }
    }

    /**
     *
     */
    public void testGetPossiblePositions() {
        setUp();

        PositionOnTrack[] positions = FlatTrackExplorer.getPossiblePositions(
                world, new Point2D(10, 10));
        assertNotNull(positions);
        assertEquals(3, positions.length);

        HashSet<TileTransition> directions = new HashSet<>();
        directions.add(TileTransition.WEST);
        directions.add(TileTransition.EAST);
        directions.add(TileTransition.SOUTH_WEST);

        HashSet<TileTransition> directions2 = new HashSet<>();

        for (PositionOnTrack position : positions) {
            directions2.add(position.cameFrom());
        }

        assertEquals(directions, directions2);
    }
}