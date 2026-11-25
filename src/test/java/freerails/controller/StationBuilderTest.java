/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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

import freerails.client.ClientProperties;
import freerails.move.MoveExecutor;
import freerails.move.Status;
import freerails.model.MapFixtureFactory2;
import freerails.move.SimpleMoveExecutor;
import freerails.util.IntPoint2D;
import freerails.model.terrain.TileTransition;
import freerails.model.world.World;
import junit.framework.TestCase;

/**
 * A Junit test.
 */
public class StationBuilderTest extends TestCase {

    private TrackMoveProducer trackBuilder;
    private StationBuilder stationBuilder;

    /**
     * Initializes the fixture before each test.
     * @throws Exception if the set up operation fails.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        World world = MapFixtureFactory2.getCopy();
        MoveExecutor moveExecutor = new SimpleMoveExecutor(world, world.getPlayer(0));
        ClientProperties properties = new ClientProperties();
        trackBuilder = new TrackMoveProducer(moveExecutor, properties);
        stationBuilder = new StationBuilder(moveExecutor);
    }

    /**
     * Tests build station behavior.
     */
    public void testBuildStation() {
        stationBuilder.setStationType(stationBuilder.getTrackTypeID("terminal"));
        TileTransition[] track = {TileTransition.EAST, TileTransition.EAST, TileTransition.EAST};

        Status status = trackBuilder.buildTrack(new IntPoint2D(10, 10), track);
        assertTrue(status.isSuccess());
        assertTrue(stationBuilder.tryBuildingStation(new IntPoint2D(10, 10)).isSuccess());
        assertTrue(stationBuilder.tryBuildingStation(new IntPoint2D(13, 10)).isSuccess());

        status = stationBuilder.buildStation(new IntPoint2D(10, 10));
        assertTrue(status.isSuccess());

        assertFalse(stationBuilder.tryBuildingStation(new IntPoint2D(13, 10)).isSuccess());
    }

    /**
     * Tests that a station on an existing station, on a tile without track, or off
     * the map is refused with a reason instead of an exception.
     */
    public void testImpossibleStationsAreRefused() {
        stationBuilder.setStationType(stationBuilder.getTrackTypeID("terminal"));
        assertTrue(trackBuilder.buildTrack(new IntPoint2D(10, 10), TileTransition.EAST).isSuccess());
        assertTrue(stationBuilder.buildStation(new IntPoint2D(10, 10)).isSuccess());

        assertEquals("There is already a station here.",
                stationBuilder.buildStation(new IntPoint2D(10, 10)).getMessage());
        assertEquals("A station must be placed on existing connected track.",
                stationBuilder.buildStation(new IntPoint2D(3, 3)).getMessage());
        assertEquals("A station must be built on the map.",
                stationBuilder.buildStation(new IntPoint2D(-1, 0)).getMessage());
    }

}
