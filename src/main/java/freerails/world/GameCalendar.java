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

package freerails.world;

import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * Converts time measured in ticks since the game began into time
 * represented as <i>Month, Year</i> and <i>hour:minute</i>.
 */
public final class GameCalendar implements Serializable {
    private static final long serialVersionUID = 3257568421033226805L;

    private static final DecimalFormat decimalFormat = new DecimalFormat("00");

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
        int i = getYear(ticks);

        return String.valueOf(i);
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
     *
     * @param i
     * @return
     */
    public String getTimeOfDay(int i) {
        int ticksPerHour = ticksPerYear / 24;
        int hour = ticksPerHour == 0 ? 0 : (i % ticksPerYear) / ticksPerHour;
        int ticksPerMinute = ticksPerYear / (24 * 60);
        int minute = ticksPerMinute == 0 ? 0 : (i % (ticksPerMinute * 60));

        return decimalFormat.format(hour) + ':' + decimalFormat.format(minute);
    }

    /**
     * @param i
     * @return
     */
    public String getYearAndMonth(int i) {
        int month = getMonth(i);
        String monthAbrev = null;

        switch (month) {
            case 0: {
                monthAbrev = "Jan";

                break;
            }

            case 1: {
                monthAbrev = "Feb";

                break;
            }

            case 2: {
                monthAbrev = "Mar";

                break;
            }

            case 3: {
                monthAbrev = "Apr";

                break;
            }

            case 4: {
                monthAbrev = "May";

                break;
            }

            case 5: {
                monthAbrev = "Jun";

                break;
            }

            case 6: {
                monthAbrev = "Jul";

                break;
            }

            case 7: {
                monthAbrev = "Aug";

                break;
            }

            case 8: {
                monthAbrev = "Sep";

                break;
            }

            case 9: {
                monthAbrev = "Oct";

                break;
            }

            case 10: {
                monthAbrev = "Nov";

                break;
            }

            case 11: {
                monthAbrev = "Dec";

                break;
            }
        }

        return monthAbrev + ' ' + getYearAsString(i);
    }

    /**
     * Returns the month, 0=Jan, 1=Feb, etc.
     *
     * @param i
     * @return
     */
    public int getMonth(int i) {
        int ticksPerMonth = ticksPerYear / 12;

        return (i % ticksPerYear) / ticksPerMonth;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof GameCalendar) {
            GameCalendar test = (GameCalendar) o;

            return this.startYear == test.startYear
                    && this.ticksPerYear == test.ticksPerYear;
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