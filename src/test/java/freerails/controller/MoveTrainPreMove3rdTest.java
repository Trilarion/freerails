/*
 * Created on 13-Aug-2005
 *
 */
package freerails.controller;

import static freerails.world.common.Step.EAST;
import static freerails.world.common.Step.NORTH;
import static freerails.world.common.Step.NORTH_EAST;
import static freerails.world.common.Step.NORTH_WEST;
import static freerails.world.common.Step.SOUTH;
import static freerails.world.common.Step.SOUTH_EAST;
import static freerails.world.common.Step.SOUTH_WEST;
import static freerails.world.common.Step.WEST;
import freerails.client.common.ModelRootImpl;
import freerails.move.MoveStatus;
import freerails.server.MapFixtureFactory2;
import freerails.world.common.ImPoint;
import freerails.world.common.PositionOnTrack;
import freerails.world.common.Step;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.top.World;
import freerails.world.track.FreerailsTile;
import junit.framework.TestCase;

/** Unit test for MoveTrainPreMove, tests pathfinding. */
public class MoveTrainPreMove3rdTest extends TestCase {

    TrackMoveProducer trackBuilder;

    StationBuilder stationBuilder;

    FreerailsPrincipal principal;

    private ImPoint stationA;

    World world;

    Step[] line1 = { EAST, NORTH_EAST, EAST, NORTH_EAST, NORTH };
    Step[] line2 = { WEST, WEST, SOUTH_WEST, SOUTH, SOUTH_EAST, EAST };
    Step[] line3 = { NORTH_WEST, NORTH_WEST, NORTH, NORTH, NORTH_EAST };

    @Override
    protected void setUp() throws Exception {
        world = MapFixtureFactory2.getCopy();
        MoveExecutor me = new SimpleMoveExecutor(world, 0);
        principal = me.getPrincipal();
        ModelRoot mr = new ModelRootImpl();
        trackBuilder = new TrackMoveProducer(me, world, mr);
        stationBuilder = new StationBuilder(me);

        // Build track.
        stationBuilder
                .setStationType(stationBuilder.getTrackTypeID("terminal"));

        stationA = new ImPoint(10, 10);
        MoveStatus ms0 = trackBuilder.buildTrack(stationA, line1);
        assertTrue(ms0.ok);
        ms0 = trackBuilder.buildTrack(stationA, line2);
        assertTrue(ms0.ok);
        ms0 = trackBuilder.buildTrack(stationA, line3);
        assertTrue(ms0.ok);

    }

    public void testFindingPath() {
        findPath2Target(new ImPoint(14, 7), line1);
        findPath2Target(new ImPoint(9, 13), line2);
        findPath2Target(new ImPoint(9, 13), line2);
    }

    private void findPath2Target(ImPoint target1, Step[] expectedPath) {
        FreerailsTile tile = (FreerailsTile) world
                .getTile(target1.x, target1.y);
        assertTrue(tile.hasTrack());
        PositionOnTrack pot = PositionOnTrack.createFacing(10, 10, EAST);
        for (int i = 0; i < expectedPath.length; i++) {
            Step expected = expectedPath[i];
            Step actual = MoveTrainPreMove.findNextStep(world, pot, target1);
            assertEquals(String.valueOf(i), expected, actual);
            pot.move(expected);
        }
    }

}
