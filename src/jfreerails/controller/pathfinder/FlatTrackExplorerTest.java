package jfreerails.controller.pathfinder;

import java.awt.Point;
import java.util.HashSet;
import jfreerails.move.ChangeTrackPieceCompositeMove;
import jfreerails.move.MoveStatus;
import jfreerails.world.common.OneTileMoveVector;
import jfreerails.world.common.PositionOnTrack;
import jfreerails.world.player.Player;
import jfreerails.world.top.GameRules;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.MapFixtureFactory;
import jfreerails.world.top.SKEY;
import jfreerails.world.top.WorldImpl;
import jfreerails.world.track.TrackRule;
import junit.framework.TestCase;


/**
 * JUnit test for FlatTrackExplorer.
 *
 * 24-Nov-2002
 * @author Luke Lindsay
 *
 */
public class FlatTrackExplorerTest extends TestCase {
    private WorldImpl world;

    public FlatTrackExplorerTest(String arg0) {
        super(arg0);
    }

    private Player testPlayer = new Player("test",
            (new Player("test")).getPublicKey(), 0);

    protected void setUp() {
        world = new WorldImpl(20, 20);
        world.addPlayer(testPlayer);
        world.set(ITEM.GAME_RULES, GameRules.NO_RESTRICTIONS);
        MapFixtureFactory.generateTrackRuleList(world);

        TrackRule rule = (TrackRule)world.get(SKEY.TRACK_RULES, 0);

        OneTileMoveVector[] vectors = {
            OneTileMoveVector.WEST, OneTileMoveVector.EAST,
            OneTileMoveVector.NORTH_EAST
        };
        Point p = new Point(10, 10);
        Point[] points = {p, p, p};

        for (int i = 0; i < points.length; i++) {
            ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove.generateBuildTrackMove(points[i],
                    vectors[i], rule, world, MapFixtureFactory.TEST_PRINCIPAL);
            MoveStatus ms = move.doMove(world, Player.AUTHORITATIVE);
            assertTrue(ms.ok);
        }
    }

    public void testGetFirstVectorToTry() {
        setUp();

        PositionOnTrack p = new PositionOnTrack(10, 10,
                OneTileMoveVector.SOUTH_WEST);
        FlatTrackExplorer fte = new FlatTrackExplorer(world, p);
        OneTileMoveVector v = fte.getFirstVectorToTry();
        assertEquals(OneTileMoveVector.EAST, v);
    }

    /** Tests that the track explorer at point 10,10 tells us
     * that we can move west, east, or northeast.
    */
    public void testGetPossibleDirections() {
        setUp();

        FlatTrackExplorer fte;

        PositionOnTrack p = new PositionOnTrack(10, 10,
                OneTileMoveVector.SOUTH_WEST);
        fte = new FlatTrackExplorer(world, p);

        //There should be 3 branches.
        assertTrue(fte.hasNextEdge());
        fte.nextEdge();
        p.setValuesFromInt(fte.getVertexConnectedByEdge());
        assertEquals(OneTileMoveVector.EAST, p.getDirection());
        assertTrue(fte.hasNextEdge());
        fte.nextEdge();

        p.setValuesFromInt(fte.getVertexConnectedByEdge());
        assertEquals(OneTileMoveVector.WEST, p.getDirection());

        assertTrue(fte.hasNextEdge());
        fte.nextEdge();
        p.setValuesFromInt(fte.getVertexConnectedByEdge());
        assertEquals(OneTileMoveVector.NORTH_EAST, p.getDirection());
        assertTrue(!fte.hasNextEdge());
    }

    /** Tests that we can move the track explorer at point 10,10
     * northeast, and that when we have done this, we can move it back again.
     */
    public void testMoveTrackExplorer() {
        setUp();

        FlatTrackExplorer fte;

        PositionOnTrack p = new PositionOnTrack(10, 10, OneTileMoveVector.EAST);
        fte = new FlatTrackExplorer(world, p);

        PositionOnTrack pos = new PositionOnTrack(fte.getPosition());
        assertEquals(10, pos.getX());
        assertEquals(10, pos.getY());
        assertTrue(fte.hasNextEdge());
        fte.nextEdge();
        pos.setValuesFromInt(fte.getVertexConnectedByEdge());
        assertEquals(OneTileMoveVector.NORTH_EAST, pos.getDirection());
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
        assertEquals(OneTileMoveVector.SOUTH_WEST,
            fte.currentBranch.getDirection());
        assertTrue(!fte.hasNextEdge());
        fte.moveForward();
        pos.setValuesFromInt(fte.getPosition());
        assertEquals(10, pos.getX());
        assertEquals(10, pos.getY());
    }

    public void testHasNext() {
        setUp();

        FlatTrackExplorer explorer = new FlatTrackExplorer(world,
                new PositionOnTrack(10, 10, OneTileMoveVector.EAST));
        assertTrue(explorer.hasNextEdge());
    }

    public void testGetPossiblePositions() {
        setUp();

        PositionOnTrack[] positions = FlatTrackExplorer.getPossiblePositions(world,
                new Point(10, 10));
        assertNotNull(positions);
        assertEquals(3, positions.length);

        HashSet directions = new HashSet();
        directions.add(OneTileMoveVector.WEST);
        directions.add(OneTileMoveVector.EAST);
        directions.add(OneTileMoveVector.SOUTH_WEST);

        HashSet directions2 = new HashSet();

        for (int i = 0; i < positions.length; i++) {
            directions2.add(positions[i].getDirection());
        }

        assertEquals(directions, directions2);
    }
}