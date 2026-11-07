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
package freerails.model.simulation;

import junit.framework.TestCase;

/**
 * Test for Clock.
 */
public class ClockTest extends TestCase {

    /**
     * Tests get year behavior.
     */
    public void testGetYear() {
        Clock clock = new Clock(1900, 10, Time.ZERO);
        assertEquals(1900, clock.getYear(new Time(0)));
        assertEquals(1900, clock.getYear(new Time(5)));
        assertEquals(1901, clock.getYear(new Time(10)));
        assertEquals(1950, clock.getYear(new Time(505)));
    }

    /**
     * Tests get year and month behavior.
     */
    public void testGetYearAndMonth() {
        Clock clock = new Clock(1900, 12, Time.ZERO);
        assertEquals("Jan 1900", clock.getDateAsString(new Time(0)));
        assertEquals("Feb 1900", clock.getDateAsString(new Time(1)));
        assertEquals("Mar 1900", clock.getDateAsString(new Time(2)));
        assertEquals("Mar 1901", clock.getDateAsString(new Time(14)));
    }

    /**
     * Tests date conversion when a year does not contain twelve equal tick
     * intervals.
     */
    public void testDateConversionWithFewerThanTwelveTicksPerYear() {
        Clock clock = new Clock(1900, 10, Time.ZERO);

        assertEquals(0, clock.getMonth(new Time(0)));
        assertEquals(10, clock.getMonth(new Time(9)));
        assertEquals("Nov 1900", clock.getDateAsString(new Time(9)));
        assertEquals("Jan 1901", clock.getDateAsString(new Time(10)));

        clock = new Clock(1900, 10, new Time(9));
        assertTrue(clock.isLastTickOfMonth());
    }

    /**
     * Tests month boundaries when the number of ticks per year is not evenly
     * divisible by twelve.
     */
    public void testIsLastTickOfMonthWithUnevenTickIntervals() {
        Clock clock = new Clock(1900, 100, new Time(7));
        assertFalse(clock.isLastTickOfMonth());

        clock = new Clock(1900, 100, new Time(8));
        assertTrue(clock.isLastTickOfMonth());

        clock = new Clock(1900, 100, new Time(9));
        assertEquals(1, clock.getCurrentMonth());
        assertFalse(clock.isLastTickOfMonth());

        clock = new Clock(1900, 100, new Time(99));
        assertTrue(clock.isLastTickOfMonth());
        assertTrue(clock.isLastTickOfYear());
    }

    /**
     * Tests advance time behavior.
     */
    public void testAdvanceTime() {
        Clock clock = new Clock(1900, 100, Time.ZERO);
        Time newTime = new Time(clock.getCurrentTime(), 100);
        clock.advanceTimeTo(newTime);
        assertEquals(newTime, clock.getCurrentTime());
        assertTrue(newTime.compareTo(clock.getCurrentTime())== 0);
    }
}