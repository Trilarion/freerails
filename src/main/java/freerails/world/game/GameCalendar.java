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

package freerails.world.game;

import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * Converts time measured in ticks since the game began into time
 * represented as <i>Month, Year</i> and <i>hour:minute</i>.
 */
public class GameCalendar implements Serializable {

    private static final long serialVersionUID = 3257568421033226805L;
    private final int ticksPerYear;
    private final int startYear;

    /**
     * @param ticksPerYear
     * @param startYear
     */
    public GameCalendar(int ticksPerYear, int startYear) {
        this.ticksPerYear = ticksPerYear;
        this.startYear = startYear;
    }

    @Override
    public int hashCode() {
        int result;
        result = ticksPerYear;
        result = 29 * result + startYear;

        return result;
    }

    /**
     * @param ticks
     * @return
     */
    public String getYearAsString(int ticks) {
        int year = getYear(ticks);
        return String.valueOf(year);
    }

    /**
     * @param ticks
     * @return
     */
    public int getYear(int ticks) {
        return startYear + (ticks / ticksPerYear);
    }

    /**
     * @param year
     * @return
     */
    public int getTicks(int year) {
        int deltaYear = year - startYear;
        return deltaYear * ticksPerYear;
    }

    /**
     * Returns the time of day as a string, note that a year is made up of a
     * representative day, so 1st June is equivalent to 12 noon.
     */
    public String getTimeOfDay(int i) {
        int ticksPerHour = ticksPerYear / 24;
        int hour = ticksPerHour == 0 ? 0 : (i % ticksPerYear) / ticksPerHour;
        int ticksPerMinute = ticksPerYear / (24 * 60);
        int minute = ticksPerMinute == 0 ? 0 : (i % (ticksPerMinute * 60));

        final DecimalFormat decimalFormat = new DecimalFormat("00");
        return decimalFormat.format(hour) + ':' + decimalFormat.format(minute);
    }

    /**
     * @param ticks
     * @return
     */
    public String getYearAndMonth(int ticks) {
        int month = getMonth(ticks);
        String shortNameOfMonth = null;

        switch (month) {
            case 0:
                shortNameOfMonth = "Jan";
                break;
            case 1:
                shortNameOfMonth = "Feb";
                break;
            case 2:
                shortNameOfMonth = "Mar";
                break;
            case 3:
                shortNameOfMonth = "Apr";
                break;
            case 4:
                shortNameOfMonth = "May";
                break;
            case 5:
                shortNameOfMonth = "Jun";
                break;
            case 6:
                shortNameOfMonth = "Jul";
                break;
            case 7:
                shortNameOfMonth = "Aug";
                break;
            case 8:
                shortNameOfMonth = "Sep";
                break;
            case 9:
                shortNameOfMonth = "Oct";
                break;
            case 10:
                shortNameOfMonth = "Nov";
                break;
            case 11:
                shortNameOfMonth = "Dec";
                break;
        }

        return shortNameOfMonth + ' ' + getYearAsString(ticks);
    }

    /**
     * Returns the month, 0=Jan, 1=Feb, etc.
     */
    public int getMonth(int i) {
        int ticksPerMonth = ticksPerYear / 12;

        return (i % ticksPerYear) / ticksPerMonth;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GameCalendar) {
            GameCalendar test = (GameCalendar) obj;

            return startYear == test.startYear && ticksPerYear == test.ticksPerYear;
        }
        return false;
    }

    /**
     * @return
     */
    public int getTicksPerYear() {
        return ticksPerYear;
    }
}