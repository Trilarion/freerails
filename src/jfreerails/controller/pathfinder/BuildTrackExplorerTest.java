/*
 * Created on Sep 3, 2004
 *
 */
package jfreerails.controller.pathfinder;

import java.awt.Point;
import jfreerails.move.ChangeTrackPieceCompositeMove;
import jfreerails.move.MoveStatus;
import jfreerails.world.common.OneTileMoveVector;
import jfreerails.world.common.PositionOnTrack;
import jfreerails.world.player.Player;
import jfreerails.world.terrain.TileTypeImpl;
import jfreerails.world.top.GameRules;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.MapFixtureFactory;
import jfreerails.world.top.SKEY;
import jfreerails.world.top.WorldImpl;
import jfreerails.world.track.FreerailsTile;
import jfreerails.world.track.TrackRule;
import junit.framework.TestCase;


/**
 * JUnit test for BuildTrackExplorer.
 * @author Luke
 *
 */
public class BuildTrackExplorerTest extends TestCase {
    private WorldImpl world;
    private Player testPlayer = new Player("test",
            (new Player("test")).getPublicKey(), 0);

    protected void setUp() throws Exception {
        world = new WorldImpl(20, 20);
        world.addPlayer(testPlayer);
        world.set(ITEM.GAME_RULES, GameRules.NO_RESTRICTIONS);
        MapFixtureFactory.generateTrackRuleList(world);
    }

    /** On a blank map, we should be able to build track in any direction as long
     * as it does not go off the map.
     */
    public void test1() {
        PositionOnTrack start;

        //Test starting in the middle of the map.
        start = new PositionOnTrack(10, 10, OneTileMoveVector.NORTH);

        BuildTrackExplorer explorer = new BuildTrackExplorer(world);
        explorer.setPosition(start.toInt());
        assertNextVertexIs(OneTileMoveVector.NORTH, 10, 9, explorer);
        assertNextVertexIs(OneTileMoveVector.NORTH_EAST, 11, 9, explorer);
        assertNextVertexIs(OneTileMoveVector.EAST, 11, 10, explorer);
        //We miss out SW, S, and SE since we don't want to double back on ourselves.
        assertNextVertexIs(OneTileMoveVector.WEST, 9, 10, explorer);
        assertNextVertexIs(OneTileMoveVector.NORTH_WEST, 9, 9, explorer);
        assertFalse(explorer.hasNextEdge());

        //Test starting in the top left of the map.
        start = new PositionOnTrack(0, 0, OneTileMoveVector.SOUTH_EAST);
        explorer.setPosition(start.toInt());
        assertNextVertexIs(OneTileMoveVector.EAST, 1, 0, explorer);
        assertNextVertexIs(OneTileMoveVector.SOUTH_EAST, 1, 1, explorer);
        assertNextVertexIs(OneTileMoveVector.SOUTH, 0, 1, explorer);
        assertFalse(explorer.hasNextEdge());

        //Test starting in the bottom right of the map.
        start = new PositionOnTrack(19, 19, OneTileMoveVector.NORTH_WEST);
        explorer.setPosition(start.toInt());
        assertNextVertexIs(OneTileMoveVector.NORTH, 19, 18, explorer);
        assertNextVertexIs(OneTileMoveVector.WEST, 18, 19, explorer);
        assertNextVertexIs(OneTileMoveVector.NORTH_WEST, 18, 18, explorer);
        assertFalse(explorer.hasNextEdge());
    }

    /** Test when we cannot build on some terrain types.*/
    public void test2() {
        //Check the the Ocean type is where we think it is.
        int occeanTypeNumber = 4;
        TileTypeImpl ocean = (TileTypeImpl)world.get(SKEY.TERRAIN_TYPES,
                occeanTypeNumber);
        assertEquals("Ocean", ocean.getTerrainCategory());

        //Check that track cannot be built on ocean.
        for (int i = 0; i < world.size(SKEY.TRACK_RULES); i++) {
            TrackRule rule = (TrackRule)world.get(SKEY.TRACK_RULES, i);
            assertFalse(rule.canBuildOnThisTerrainType(
                    ocean.getTerrainCategory()));
        }

        //Place some occean.
        FreerailsTile tile = FreerailsTile.getInstance(occeanTypeNumber);
        world.setTile(10, 9, tile);
        world.setTile(11, 10, tile);

        PositionOnTrack start;

        //Test starting in the middle of the map.
        start = new PositionOnTrack(10, 10, OneTileMoveVector.NORTH);

        BuildTrackExplorer explorer = new BuildTrackExplorer(world);
        explorer.setPosition(start.toInt());
        assertNextVertexIs(OneTileMoveVector.NORTH_EAST, 11, 9, explorer);
        //We miss out SW, S, and SE since we don't want to double back on ourselves.
        assertNextVertexIs(OneTileMoveVector.WEST, 9, 10, explorer);
        assertNextVertexIs(OneTileMoveVector.NORTH_WEST, 9, 9, explorer);
        assertFalse(explorer.hasNextEdge());
    }

    /** Test for illegal track configurations.*/
    public void test3() {
        //Build some track, from 10, 10 diagonally SE. 
        int y = 10;
        int x = 10;

        for (int i = 0; i < 4; i++) {
            OneTileMoveVector v = OneTileMoveVector.SOUTH_EAST;
            buildTrack(x, y, v);
            x += v.deltaX;
            y += v.deltaY;
        }

        //If we enter 10, 10 from the south, we should be able to build track S & SW.
        PositionOnTrack start = new PositionOnTrack(10, 10,
                OneTileMoveVector.SOUTH);
        BuildTrackExplorer explorer = new BuildTrackExplorer(world);
        explorer.setPosition(start.toInt());
        //SE is going along existing track
        assertNextVertexIs(OneTileMoveVector.SOUTH_EAST, 11, 11, explorer);
        //S is building new track.
        assertNextVertexIs(OneTileMoveVector.SOUTH, 10, 11, explorer);
        assertFalse(explorer.hasNextEdge());

        //If we enter 10, 11 from the north, we should be able to build track N, E, W, & NW.
        start = new PositionOnTrack(10, 11, OneTileMoveVector.NORTH);
        explorer.setPosition(start.toInt());
        assertNextVertexIs(OneTileMoveVector.NORTH, 10, 10, explorer);
        assertNextVertexIs(OneTileMoveVector.EAST, 11, 11, explorer);
        assertNextVertexIs(OneTileMoveVector.WEST, 9, 11, explorer);
        assertNextVertexIs(OneTileMoveVector.NORTH_WEST, 9, 10, explorer);
        assertFalse(explorer.hasNextEdge());

        //If we enter 10, 12 from the north, we also should be able to build track N, E, W, & NW.
        start = new PositionOnTrack(10, 12, OneTileMoveVector.NORTH);
        explorer.setPosition(start.toInt());
        assertNextVertexIs(OneTileMoveVector.NORTH, 10, 11, explorer);
        assertNextVertexIs(OneTileMoveVector.EAST, 11, 12, explorer);
        assertNextVertexIs(OneTileMoveVector.WEST, 9, 12, explorer);
        assertNextVertexIs(OneTileMoveVector.NORTH_WEST, 9, 11, explorer);
        assertFalse(explorer.hasNextEdge());
    }

    private void assertNextVertexIs(OneTileMoveVector oneTileMoveVector, int x,
        int y, BuildTrackExplorer explorer) {
        assertTrue(explorer.hasNextEdge());
        explorer.nextEdge();

        PositionOnTrack pos = new PositionOnTrack(explorer.getVertexConnectedByEdge());
        assertEquals(new PositionOnTrack(x, y, oneTileMoveVector), pos);
    }

    private void buildTrack(int x, int y, OneTileMoveVector direction) {
        TrackRule rule = (TrackRule)world.get(SKEY.TRACK_RULES, 0);
        ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove.generateBuildTrackMove(new Point(
                    x, y), direction, rule, world,
                MapFixtureFactory.TEST_PRINCIPAL);
        MoveStatus ms = move.doMove(world, Player.AUTHORITATIVE);
        assertTrue(ms.ok);
    }
}