/*
 * Created on 24-Dec-2004
 *
 */
package jfreerails.controller;

import static jfreerails.world.common.Step.EAST;
import jfreerails.client.common.ModelRootImpl;
import jfreerails.move.MoveStatus;
import jfreerails.server.MapFixtureFactory2;
import jfreerails.world.common.ImPoint;
import jfreerails.world.common.Step;
import jfreerails.world.top.World;
import junit.framework.TestCase;

/**
 * A Junit test.
 * 
 * @author Luke Lindsay
 * 
 * 
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
        Step[] track = { EAST, EAST, EAST };
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
