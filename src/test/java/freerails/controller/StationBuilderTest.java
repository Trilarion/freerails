/*
 * Created on 24-Dec-2004
 *
 */
package freerails.controller;

import freerails.client.common.ModelRootImpl;
import freerails.move.MoveStatus;
import freerails.server.MapFixtureFactory2;
import freerails.world.common.ImPoint;
import freerails.world.common.Step;
import freerails.world.top.World;
import junit.framework.TestCase;

import static freerails.world.common.Step.EAST;

/**
 * A Junit test.
 *
 * @author Luke Lindsay
 */
public class StationBuilderTest extends TestCase {

    World w;

    TrackMoveProducer trackBuilder;

    StationBuilder stationBuilder;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        w = MapFixtureFactory2.getCopy();
        MoveExecutor me = new SimpleMoveExecutor(w, 0);
        ModelRoot mr = new ModelRootImpl();
        trackBuilder = new TrackMoveProducer(me, w, mr);
        stationBuilder = new StationBuilder(me);
    }

    public void testCanBuiltStationHere() {

    }

    public void testBuildStation() {
        stationBuilder
                .setStationType(stationBuilder.getTrackTypeID("terminal"));
        Step[] track = {EAST, EAST, EAST};
        MoveStatus ms = trackBuilder.buildTrack(new ImPoint(10, 10), track);
        assertTrue(ms.ok);
        assertTrue(stationBuilder.tryBuildingStation(new ImPoint(10, 10)).ok);
        assertTrue(stationBuilder.tryBuildingStation(new ImPoint(13, 10)).ok);
        MoveStatus ms1 = stationBuilder.buildStation(new ImPoint(10, 10));
        assertTrue(ms1.ok);

        MoveStatus ms2 = stationBuilder.buildStation(new ImPoint(13, 10));
        assertFalse(ms2.ok);
    }

}
