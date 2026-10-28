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

package freerails.model.game;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

// TODO store speed separately in World
/**
 * Tracks elapsed simulation time and converts ticks to calendar years and
 * months.
 */
public class Clock implements Serializable {

    private final int startYear;
    private final int ticksPerYear;
    private Time currentTime;

    /**
     * Creates a clock with the supplied calendar origin and current time.
     *
     * @param startYear calendar year at tick zero
     * @param ticksPerYear number of simulation ticks in a year; must be positive
     * @param currentTime initial elapsed simulation time
     * @throws IllegalArgumentException if {@code ticksPerYear} is not positive
     */
    public Clock(int startYear, int ticksPerYear, @NotNull Time currentTime) {
        if (ticksPerYear <= 0) {
            throw new IllegalArgumentException();
        }
        this.startYear = startYear;
        this.ticksPerYear = ticksPerYear;
        this.currentTime = currentTime;
    }

    /** Returns the calendar year assigned to tick zero. */
    public int getStartYear() {
        return startYear;
    }

    /** Returns the current elapsed simulation time. */
    public Time getCurrentTime() {
        return currentTime;
    }

    /** Advances the clock by one simulation tick. */
    public void advanceTime() {
        currentTime = currentTime.advance();
    }

    /**
     * Advances the clock until it reaches the specified time.
     *
     * @param time target time, which must not precede the current time
     * @throws IllegalArgumentException if the target is earlier than the current time
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
     * Returns the calendar year containing the current time.
     *
     * @return the current calendar year
     */
    public int getCurrentYear() {
        return getYear(currentTime);
    }

    /**
     * Calculates the calendar year containing an arbitrary elapsed time.
     *
     * @param time elapsed time to convert
     * @return the corresponding calendar year
     */
    public int getYear(@NotNull Time time) {
        return startYear + time.ticks() / ticksPerYear;
    }


    /**
     * Calculates the elapsed time at the beginning of a calendar year.
     *
     * @param year calendar year, not earlier than the clock's start year
     * @return elapsed time at the beginning of {@code year}
     * @throws IllegalArgumentException if {@code year} precedes the start year
     */
    public Time getTimeAtStartOfYear(int year) {
        if (year < startYear) {
            throw new IllegalArgumentException();
        }
        return new Time((year - startYear) * ticksPerYear);
    }

    /**
     * Returns the elapsed time at the beginning of the current calendar year.
     *
     * @return start-of-year time for the current year
     */
    public Time getTimeAtStartOfCurrentYear() {
        return getTimeAtStartOfYear(getCurrentYear());
    }

    /** @return {@code true} if the current tick is the final tick of its year */
    public boolean isLastTickOfYear() {
        return currentTime.ticks() % ticksPerYear == ticksPerYear - 1;
    }

    // TODO a better way of calculating this
    /**
     * Checks whether this tick and the following tick have the same computed
     * month index.
     *
     * @return {@code true} when the month index for the next tick matches the
     *         current month index
     */
    public boolean isLastTickOfMonth() {
        return getCurrentMonth() == (currentTime.ticks()+1) % ticksPerYear / (ticksPerYear / 12);
    }

    /**
     * Calculates the month (starting with 0 to 11) that we are in.
     *
     * @return the current month as a zero-based value from 0 through 11
     */
    public int getCurrentMonth() {
        return getMonth(currentTime);
    }

    /**
     * Calculates the zero-based month containing the specified elapsed time.
     *
     * @param time elapsed time to convert
     * @return month index from 0 (January) through 11 (December)
     */
    public int getMonth(@NotNull Time time) {
        return time.ticks() % ticksPerYear / (ticksPerYear / 12);
    }

    private static String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    // TODO is there a library for it similar to datetime in Python?
    /**
     * Converts the current date to the abbreviated format {@code "MMM YYYY"}.
     *
     * @return current month and year, for example {@code "Jan 1840"}
     */
    public String getCurrentDateAsString() {
        return String.format("%s %s", MONTHS[getCurrentMonth()], getCurrentYear());
    }

    /**
     * Formats an arbitrary elapsed time as an abbreviated month and year.
     *
     * @param time elapsed time to format
     * @return date in {@code "MMM YYYY"} format
     */
    public  String getDateAsString(@NotNull Time time) {
        return String.format("%s %s", MONTHS[getMonth(time)], getYear(time));
    }

    /** Returns the number of simulation ticks per calendar year. */
    public int getTicksPerYear() {
        return ticksPerYear;
    }

    /**
     * Compares calendar origin, tick rate, and current time.
     *
     * @param obj object to compare
     * @return {@code true} if the other object is an equivalent clock
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Clock o)) {
            return false;
        }
        return Objects.equals(startYear, o.startYear) && Objects.equals(ticksPerYear, o.ticksPerYear) && Objects.equals(currentTime, o.currentTime);
    }

    /** Returns a hash code consistent with {@link #equals(Object)}. */
    @Override
    public int hashCode() {
        int result = startYear;
        result = 31 * result + ticksPerYear;
        result = 31 * result + currentTime.hashCode();
        return result;
    }
}
