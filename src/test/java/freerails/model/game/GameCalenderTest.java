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
 * Test for GameCalendar.
 */
public class GameCalenderTest extends TestCase {

    /**
     *
     */
    public void testGetYear() {
        GameCalendar calendar = new GameCalendar(10, 1900);
        assertEquals("1900", calendar.getYearAsString(0));
        assertEquals("1900", calendar.getYearAsString(5));
        assertEquals("1901", calendar.getYearAsString(10));
        assertEquals("1950", calendar.getYearAsString(505));
    }

    /**
     *
     */
    public void testGetTimeOfDay() {
        GameCalendar calendar = new GameCalendar(24, 1900);
        assertEquals("00:00", calendar.getTimeOfDay(0));
        assertEquals("01:00", calendar.getTimeOfDay(1));
        assertEquals("15:00", calendar.getTimeOfDay(15));

        calendar = new GameCalendar(24 * 60, 1900);
        assertEquals("00:00", calendar.getTimeOfDay(0));
        assertEquals("00:10", calendar.getTimeOfDay(10));
        assertEquals("05:10", calendar.getTimeOfDay(310));
    }

    /**
     *
     */
    public void testGetYearAndMonth() {
        GameCalendar calendar = new GameCalendar(12, 1900);
        assertEquals("Jan 1900", calendar.getYearAndMonth(0));
        assertEquals("Feb 1900", calendar.getYearAndMonth(1));
        assertEquals("Mar 1900", calendar.getYearAndMonth(2));
        assertEquals("Mar 1901", calendar.getYearAndMonth(14));
    }
}