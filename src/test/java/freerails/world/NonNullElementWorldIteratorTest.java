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
package freerails.world;

import freerails.util.Vector2D;
import freerails.world.station.Station;
import freerails.world.world.FullWorld;
import freerails.world.world.World;
import junit.framework.TestCase;

import java.util.NoSuchElementException;

/**
 * Tests NonNullElementWorldIterator.
 */
public class NonNullElementWorldIteratorTest extends TestCase {

    private World world;
    private Station station1;
    private Station station2;

    /**
     *
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        world = new FullWorld();
        station1 = new Station(new Vector2D(10, 20), "Station1", 4, 0);
        station2 = new Station(new Vector2D(15, 16), "Station2", 4, 1);
        Station station3 = new Station(new Vector2D(30, 50), "Station3", 4, 2);
        world.addPlayer(MapFixtureFactory.TEST_PLAYER);
        world.add(MapFixtureFactory.TEST_PRINCIPAL, KEY.STATIONS, station1);
        world.add(MapFixtureFactory.TEST_PRINCIPAL, KEY.STATIONS, null);
        world.add(MapFixtureFactory.TEST_PRINCIPAL, KEY.STATIONS, station2);
        world.add(MapFixtureFactory.TEST_PRINCIPAL, KEY.STATIONS, null);
        world.add(MapFixtureFactory.TEST_PRINCIPAL, KEY.STATIONS, null);
        world.add(MapFixtureFactory.TEST_PRINCIPAL, KEY.STATIONS, station3);
    }

    /**
     *
     */
    public void testNext() {
        WorldIterator wi = new NonNullElementWorldIterator(KEY.STATIONS, world,
                MapFixtureFactory.TEST_PRINCIPAL);
        assertEquals(WorldIterator.BEFORE_FIRST, wi.getRowID());
        assertEquals(WorldIterator.BEFORE_FIRST, wi.getIndex());

        // Look at first station
        boolean b = wi.next();
        assertTrue(b);

        int index = wi.getIndex();
        assertEquals(0, index);
        assertEquals(0, wi.getRowID());
        assertEquals(station1, wi.getElement());

        // Look at second station
        assertTrue(wi.next());
        assertEquals(2, wi.getIndex());
        assertEquals(1, wi.getRowID());
        assertEquals(station2, wi.getElement());

        WorldIterator wi2 = new NonNullElementWorldIterator(SKEY.TRACK_RULES, world);
        assertTrue(!wi2.next());
    }

    /**
     *
     */
    public void testGotoIndex() {
        WorldIterator worldIterator = new NonNullElementWorldIterator(KEY.STATIONS, world,
                MapFixtureFactory.TEST_PRINCIPAL);
        assertEquals(WorldIterator.BEFORE_FIRST, worldIterator.getRowID());
        assertEquals(WorldIterator.BEFORE_FIRST, worldIterator.getIndex());

        worldIterator.gotoIndex(2);
        assertEquals(2, worldIterator.getIndex());
        assertEquals(1, worldIterator.getRowID());

        try {
            worldIterator.gotoIndex(100);
            assertTrue(false);
        } catch (NoSuchElementException e) {
        }
    }
}