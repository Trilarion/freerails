/*
 * Created on 28-May-2003
 *
 */
package freerails.world.common;

import junit.framework.TestCase;

/**
 * Junit test for GameCalendar.
 * 
 * @author Luke
 * 
 */
public class GameCalenderTest extends TestCase {
    public void testGetYear() {
        GameCalendar gc = new GameCalendar(10, 1900);
        assertEquals("1900", gc.getYearAsString(0));
        assertEquals("1900", gc.getYearAsString(5));
        assertEquals("1901", gc.getYearAsString(10));
        assertEquals("1950", gc.getYearAsString(505));
    }

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

    public void testGetYearAndMonth() {
        GameCalendar gc = new GameCalendar(12, 1900);
        assertEquals("Jan 1900", gc.getYearAndMonth(0));
        assertEquals("Feb 1900", gc.getYearAndMonth(1));
        assertEquals("Mar 1900", gc.getYearAndMonth(2));
        assertEquals("Mar 1901", gc.getYearAndMonth(14));
    }
}