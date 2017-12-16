package freerails.controller;

import java.util.HashSet;

import freerails.move.ChangeTrackPieceCompositeMove;
import freerails.move.MoveStatus;
import freerails.world.common.ImPoint;
import freerails.world.common.PositionOnTrack;
import freerails.world.common.Step;
import freerails.world.player.Player;
import freerails.world.top.GameRules;
import freerails.world.top.ITEM;
import freerails.world.top.MapFixtureFactory;
import freerails.world.top.SKEY;
import freerails.world.top.WorldImpl;
import freerails.world.track.TrackRule;
import junit.framework.TestCase;

/**
 * JUnit test for FlatTrackExplorer.
 * 
 * 24-Nov-2002
 * 
 * @author Luke Lindsay
 * 
 */
public class FlatTrackExplorerTest extends TestCase {
    private WorldImpl world;

    public FlatTrackExplorerTest(String arg0) {
        super(arg0);
    }

    private Player testPlayer = new Player("test", 0);

    @Override
    protected void setUp() {
        world = new WorldImpl(20, 20);
        world.addPlayer(testPlayer);
        world.set(ITEM.GAME_RULES, GameRules.NO_RESTRICTIONS);
        MapFixtureFactory.generateTrackRuleList(world);

        TrackRule rule = (TrackRule) world.get(SKEY.TRACK_RULES, 0);

        Step[] vectors = { Step.WEST, Step.EAST, Step.NORTH_EAST };
        ImPoint p = new ImPoint(10, 10);
        ImPoint[] points = { p, p, p };

        for (int i = 0; i < points.length; i++) {
            ChangeTrackPieceCompositeMove move = ChangeTrackPieceCompositeMove
                    .generateBuildTrackMove(points[i], vectors[i], rule, rule,
                            world, MapFixtureFactory.TEST_PRINCIPAL);
            MoveStatus ms = move.doMove(world, Player.AUTHORITATIVE);
            assertTrue(ms.ok);
        }
    }

    public void testGetFirstVectorToTry() throws NoTrackException {
        setUp();

        PositionOnTrack p = PositionOnTrack.createComingFrom(10, 10,
                Step.SOUTH_WEST);
        FlatTrackExplorer fte = new FlatTrackExplorer(world, p);
        Step v = fte.getFirstVectorToTry();
        assertEquals(Step.EAST, v);
    }

    /**
     * Tests that the track explorer at point 10,10 tells us that we can move
     * west, east, or northeast.
     * 
     * @throws NoTrackException
     */
    public void testGetPossibleDirections() throws NoTrackException {
        setUp();

        FlatTrackExplorer fte;

        PositionOnTrack p = PositionOnTrack.createComingFrom(10, 10,
                Step.SOUTH_WEST);
        fte = new FlatTrackExplorer(world, p);

        // There should be 3 branches.
        assertTrue(fte.hasNextEdge());
        fte.nextEdge();
        p.setValuesFromInt(fte.getVertexConnectedByEdge());
        assertEquals(Step.EAST, p.cameFrom());
        assertTrue(fte.hasNextEdge());
        fte.nextEdge();

        p.setValuesFromInt(fte.getVertexConnectedByEdge());
        assertEquals(Step.WEST, p.cameFrom());

        assertTrue(fte.hasNextEdge());
        fte.nextEdge();
        p.setValuesFromInt(fte.getVertexConnectedByEdge());
        assertEquals(Step.NORTH_EAST, p.cameFrom());
        assertTrue(!fte.hasNextEdge());
    }

    /**
     * Tests that we can move the track explorer at point 10,10 northeast, and
     * that when we have done this, we can move it back again.
     * 
     * @throws NoTrackException
     */
    public void testMoveTrackExplorer() throws NoTrackException {
        setUp();

        FlatTrackExplorer fte;

        PositionOnTrack p = PositionOnTrack.createComingFrom(10, 10, Step.EAST);
        fte = new FlatTrackExplorer(world, p);

        PositionOnTrack pos = new PositionOnTrack(fte.getPosition());
        assertEquals(10, pos.getX());
        assertEquals(10, pos.getY());
        assertTrue(fte.hasNextEdge());
        fte.nextEdge();
        pos.setValuesFromInt(fte.getVertexConnectedByEdge());
        assertEquals(Step.NORTH_EAST, pos.cameFrom());
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
        assertEquals(Step.SOUTH_WEST, fte.currentBranch.cameFrom());
        assertTrue(!fte.hasNextEdge());
        fte.moveForward();
        pos.setValuesFromInt(fte.getPosition());
        assertEquals(10, pos.getX());
        assertEquals(10, pos.getY());
    }

    public void testHasNext() throws NoTrackException {
        setUp();

        FlatTrackExplorer explorer = new FlatTrackExplorer(world,
                PositionOnTrack.createComingFrom(10, 10, Step.EAST));
        assertTrue(explorer.hasNextEdge());
    }

    public void testNoTrack() {
        setUp();

        try {
            FlatTrackExplorer explorer = new FlatTrackExplorer(world,
                    PositionOnTrack.createComingFrom(4, 7, Step.EAST));
            fail("Expected an Exception");
        } catch (NoTrackException e) {
            // ignore
        }
    }

    public void testGetPossiblePositions() {
        setUp();

        PositionOnTrack[] positions = FlatTrackExplorer.getPossiblePositions(
                world, new ImPoint(10, 10));
        assertNotNull(positions);
        assertEquals(3, positions.length);

        HashSet<Step> directions = new HashSet<Step>();
        directions.add(Step.WEST);
        directions.add(Step.EAST);
        directions.add(Step.SOUTH_WEST);

        HashSet<Step> directions2 = new HashSet<Step>();

        for (int i = 0; i < positions.length; i++) {
            directions2.add(positions[i].cameFrom());
        }

        assertEquals(directions, directions2);
    }
}