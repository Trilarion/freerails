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
package freerails.model.game;

import junit.framework.TestCase;

/**
 * Test for Clock.
 */
public class ClockTest extends TestCase {

    /**
     *
     */
    public void testGetYear() {
        Clock clock = new Clock(1900, 10, Time.ZERO);
        assertEquals(1900, clock.getYear(new Time(0)));
        assertEquals(1900, clock.getYear(new Time(5)));
        assertEquals(1901, clock.getYear(new Time(10)));
        assertEquals(1950, clock.getYear(new Time(505)));
    }

    /**
     *
     */
    public void testGetYearAndMonth() {
        Clock clock = new Clock(1900, 12, Time.ZERO);
        assertEquals("Jan 1900", clock.getDateAsString(new Time(0)));
        assertEquals("Feb 1900", clock.getDateAsString(new Time(1)));
        assertEquals("Mar 1900", clock.getDateAsString(new Time(2)));
        assertEquals("Mar 1901", clock.getDateAsString(new Time(14)));
    }

    public void testAdvanceTime() {
        Clock clock = new Clock(1900, 100, Time.ZERO);
        Time newTime = new Time(clock.getCurrentTime(), 100);
        clock.advanceTimeTo(newTime);
        assertEquals(newTime, clock.getCurrentTime());
        assertTrue(newTime.compareTo(clock.getCurrentTime())== 0);
    }
}