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
package freerails.world.top;

import freerails.world.*;
import freerails.world.station.Station;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.NoSuchElementException;

/**
 * This junit TestCase tests NonNullElementWorldIterator.
 */
public class NonNullElementWorldIteratorTest extends TestCase {
    private World w;

    private Station station1;

    private Station station2;

    private Station station3;

    /**
     * @param args
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * @return
     */
    public static Test suite() {

        return new TestSuite(NonNullElementWorldIteratorTest.class);
    }

    /**
     *
     */
    @Override
    protected void setUp() {
        w = new WorldImpl();
        station1 = new Station(10, 20, "Station1", 4, 0);
        station2 = new Station(15, 16, "Station2", 4, 1);
        station3 = new Station(30, 50, "Station3", 4, 2);
        w.addPlayer(MapFixtureFactory.TEST_PLAYER);
        w.add(MapFixtureFactory.TEST_PRINCIPAL, KEY.STATIONS, station1);
        w.add(MapFixtureFactory.TEST_PRINCIPAL, KEY.STATIONS, null);
        w.add(MapFixtureFactory.TEST_PRINCIPAL, KEY.STATIONS, station2);
        w.add(MapFixtureFactory.TEST_PRINCIPAL, KEY.STATIONS, null);
        w.add(MapFixtureFactory.TEST_PRINCIPAL, KEY.STATIONS, null);
        w.add(MapFixtureFactory.TEST_PRINCIPAL, KEY.STATIONS, station3);
    }

    /**
     *
     */
    public void testNext() {
        WorldIterator wi = new NonNullElementWorldIterator(KEY.STATIONS, w,
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

        WorldIterator wi2 = new NonNullElementWorldIterator(SKEY.TRACK_RULES, w);
        assertTrue(!wi2.next());
    }

    /**
     *
     */
    public void testGotoIndex() {
        WorldIterator wi = new NonNullElementWorldIterator(KEY.STATIONS, w,
                MapFixtureFactory.TEST_PRINCIPAL);
        assertEquals(WorldIterator.BEFORE_FIRST, wi.getRowID());
        assertEquals(WorldIterator.BEFORE_FIRST, wi.getIndex());

        wi.gotoIndex(2);
        assertEquals(2, wi.getIndex());
        assertEquals(1, wi.getRowID());

        try {
            wi.gotoIndex(100);
            assertTrue(false);
        } catch (NoSuchElementException e) {
        }
    }
}