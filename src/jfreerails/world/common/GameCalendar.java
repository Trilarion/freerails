package jfreerails.world.common;

import java.text.DecimalFormat;


/** This class converts time meansured in ticks since the game began into time represented
 * as <i>Month, Year</i> and <i>hour:minute</i>.
 */
final public class GameCalendar implements FreerailsSerializable {
    private static DecimalFormat decimalFormat = new DecimalFormat("00");
    private final int ticksPerYear;
    private final int startYear;

    public String getYearAsString(int ticks) {
        int i = getYear(ticks);

        return String.valueOf(i);
    }

    public int getYear(int ticks) {
        return startYear + (ticks / ticksPerYear);
    }

    /** Returns the time of day as a string, note that a year is made
     * up of a representative day, so 1st June is equilavent to 12 noon.
     */
    public String getTimeOfDay(int i) {
        int ticksPerHour = ticksPerYear / 24;
        int hour = ticksPerHour == 0 ? 0 : (i % ticksPerYear) / ticksPerHour;
        int ticksPerMinute = ticksPerYear / (24 * 60);
        int minute = ticksPerMinute == 0 ? 0 : (i % (ticksPerMinute * 60));

        return decimalFormat.format(hour) + ":" + decimalFormat.format(minute);
    }

    public String getYearAndMonth(int i) {
        int ticksPerMonth = ticksPerYear / 12;
        int month = getMonth(i, ticksPerMonth);
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

        return monthAbrev + " " + getYearAsString(i);
    }

    public int getMonth(int i, int ticksPerMonth) {
        return (i % ticksPerYear) / ticksPerMonth;
    }

    public GameCalendar(int ticksPerYear, int startYear) {
        this.ticksPerYear = ticksPerYear;
        this.startYear = startYear;
    }

    public boolean equals(Object o) {
        if (o instanceof GameCalendar) {
            GameCalendar test = (GameCalendar)o;

            if (this.startYear != test.startYear ||
                    this.ticksPerYear != test.ticksPerYear) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }
}