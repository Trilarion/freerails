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

package freerails.model.game;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

// TODO store speed separately in World
/**
 *
 */
public class Clock implements Serializable {

    private final int startYear;
    private final int ticksPerYear;
    private Time currentTime;

    public Clock(int startYear, int ticksPerYear, @NotNull Time currentTime) {
        if (ticksPerYear <= 0) {
            throw new IllegalArgumentException();
        }
        this.startYear = startYear;
        this.ticksPerYear = ticksPerYear;
        this.currentTime = currentTime;
    }

    public int getStartYear() {
        return startYear;
    }

    public Time getCurrentTime() {
        return currentTime;
    }

    public void advanceTime() {
        currentTime = currentTime.advance();
    }

    /**
     * Convenience function
     *
     * @param time
     */
    public void advanceTimeTo(@NotNull Time time) {
        if (currentTime.compareTo(time) > 0) {
            throw new IllegalArgumentException();
        }
        while (currentTime.compareTo(time) < 0) {
            advanceTime();
        }

    }

    /**
     * Calculates the actual year.
     *
     * @return
     */
    public int getCurrentYear() {
        return getYear(currentTime);
    }

    /**
     * Calculates the year of an arbitrary currentTime.
     *
     * @param time
     * @return
     */
    public int getYear(@NotNull Time time) {
        return startYear + time.getTicks() / ticksPerYear;
    }


    /**
     * Calculates the currentTime at the beginning of a given year.
     *
     * @param year
     * @return
     */
    public Time getTimeAtStartOfYear(int year) {
        if (year < startYear) {
            throw new IllegalArgumentException();
        }
        return new Time((year - startYear) * ticksPerYear);
    }

    /**
     * Convenience function.
     *
     * @return
     */
    public Time getTimeAtStartOfCurrentYear() {
        return getTimeAtStartOfYear(getCurrentYear());
    }

    /**
     *
     * @return
     */
    public boolean isLastTickOfYear() {
        return currentTime.getTicks() % ticksPerYear == ticksPerYear - 1;
    }

    // TODO a better way of calculating this
    public boolean isLastTickOfMonth() {
        return getCurrentMonth() == ((currentTime.getTicks()+1) % ticksPerYear) / (ticksPerYear / 12);
    }

    /**
     * Calculates the month (starting with 0 to 11) that we are in.
     *
     * @return
     */
    public int getCurrentMonth() {
        return getMonth(currentTime);
    }

    public int getMonth(@NotNull Time time) {
        return (time.getTicks() % ticksPerYear) / (ticksPerYear / 12);
    }

    private static String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    // TODO is there a library for it similar to datetime in Python?
    /**
     * Converts the current date to a string of format "MM YYYY".
     *
     * @return
     */
    public String getCurrentDateAsString() {
        return String.format("%s %s", MONTHS[getCurrentMonth()], getCurrentYear());
    }

    public  String getDateAsString(@NotNull Time time) {
        return String.format("%s %s", MONTHS[getMonth(time)], getYear(time));
    }

    public int getTicksPerYear() {
        return ticksPerYear;
    }
}
