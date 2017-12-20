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
package freerails.world.common;

import freerails.world.GameCalendar;
import junit.framework.TestCase;

/**
 * Junit test for GameCalendar.
 */
public class GameCalenderTest extends TestCase {

    /**
     *
     */
    public void testGetYear() {
        GameCalendar gc = new GameCalendar(10, 1900);
        assertEquals("1900", gc.getYearAsString(0));
        assertEquals("1900", gc.getYearAsString(5));
        assertEquals("1901", gc.getYearAsString(10));
        assertEquals("1950", gc.getYearAsString(505));
    }

    /**
     *
     */
    public void testGetTimeOfDay() {
        GameCalendar gc = new GameCalendar(24, 1900);
        assertEquals("00:00", gc.getTimeOfDay(0));
        assertEquals("01:00", gc.getTimeOfDay(1));
        assertEquals("15:00", gc.getTimeOfDay(15));

        gc = new GameCalendar(24 * 60, 1900);
        assertEquals("00:00", gc.getTimeOfDay(0));
        assertEquals("00:10", gc.getTimeOfDay(10));
        assertEquals("05:10", gc.getTimeOfDay(310));
    }

    /**
     *
     */
    public void testGetYearAndMonth() {
        GameCalendar gc = new GameCalendar(12, 1900);
        assertEquals("Jan 1900", gc.getYearAndMonth(0));
        assertEquals("Feb 1900", gc.getYearAndMonth(1));
        assertEquals("Mar 1900", gc.getYearAndMonth(2));
        assertEquals("Mar 1901", gc.getYearAndMonth(14));
    }
}