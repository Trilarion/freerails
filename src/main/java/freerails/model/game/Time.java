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

/**
 * An immutable non-negative count of elapsed simulation ticks.
 */
public record Time(int ticks) implements Comparable<Time>, Serializable {

    public static final Time ZERO = new Time(0);
    public static final Time INFINITY = new Time(Integer.MAX_VALUE);

    /**
     * Creates a time from an absolute tick count.
     *
     * @param ticks elapsed ticks; must be non-negative
     * @throws IllegalArgumentException if {@code ticks} is negative
     */
    public Time {
        if (ticks < 0) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Creates a time by adding a tick offset to another time.
     *
     * @param time       base time
     * @param deltaTicks signed number of ticks to add
     * @throws IllegalArgumentException if the resulting time is negative
     */
    public Time(@NotNull Time time, int deltaTicks) {
        this(time.ticks() + deltaTicks);
    }

    /**
     * Returns the elapsed tick count.
     */
    @Override
    public int ticks() {
        return ticks;
    }

    /**
     * Returns a new time one tick after this time.
     *
     * @return the next simulation time
     */
    public Time advance() {
        return new Time(this, 1);
    }

    /**
     * Compares elapsed tick counts.
     *
     * @param o time to compare with
     * @return a negative value, zero, or a positive value as this time is
     * earlier than, equal to, or later than {@code o}
     */
    @Override
    public int compareTo(@NotNull Time o) {
        return Integer.compare(ticks, o.ticks);
    }

    /**
     * Compares this time with another object by elapsed tick count.
     *
     * @param obj object to compare
     * @return {@code true} if {@code obj} is an equal {@code Time}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Time o)) {
            return false;
        }
        return compareTo(o) == 0;
    }

    /**
     * Returns the elapsed tick count as a decimal string.
     */
    @Override
    public String toString() {
        return String.valueOf(ticks);
    }
}
