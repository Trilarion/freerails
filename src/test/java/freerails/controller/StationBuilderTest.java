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

import freerails.client.common.ModelRootImpl;
import freerails.move.MoveStatus;
import freerails.server.MapFixtureFactory2;
import freerails.util.Point2D;
import freerails.world.terrain.TileTransition;
import freerails.world.World;
import junit.framework.TestCase;

/**
 * A Junit test.
 */
public class StationBuilderTest extends TestCase {

    World w;

    TrackMoveProducer trackBuilder;

    StationBuilder stationBuilder;

    /**
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        w = MapFixtureFactory2.getCopy();
        MoveExecutor me = new SimpleMoveExecutor(w, 0);
        ModelRoot mr = new ModelRootImpl();
        trackBuilder = new TrackMoveProducer(me, w, mr);
        stationBuilder = new StationBuilder(me);
    }

    /**
     *
     */
    public void testBuildStation() {
        stationBuilder
                .setStationType(stationBuilder.getTrackTypeID("terminal"));
        TileTransition[] track = {TileTransition.EAST, TileTransition.EAST, TileTransition.EAST};
        MoveStatus ms = trackBuilder.buildTrack(new Point2D(10, 10), track);
        assertTrue(ms.ok);
        assertTrue(stationBuilder.tryBuildingStation(new Point2D(10, 10)).ok);
        assertTrue(stationBuilder.tryBuildingStation(new Point2D(13, 10)).ok);
        MoveStatus ms1 = stationBuilder.buildStation(new Point2D(10, 10));
        assertTrue(ms1.ok);

        MoveStatus ms2 = stationBuilder.buildStation(new Point2D(13, 10));
        assertFalse(ms2.ok);
    }

}
