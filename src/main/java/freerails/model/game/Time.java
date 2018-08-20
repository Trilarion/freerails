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

/**
 *
 */
public class Time implements Comparable<Time>, Serializable {

    public static final Time ZERO = new Time(0);

    private final int ticks;

    public Time(int ticks) {
        if (ticks < 0) {
            throw new IllegalArgumentException();
        }
        this.ticks = ticks;
    }

    public Time(@NotNull Time time, int deltaTicks) {
        int ticks = time.getTicks() + deltaTicks;
        if (ticks < 0) {
            throw new IllegalArgumentException();
        }
        this.ticks = ticks;
    }

    public int getTicks() {
        return ticks;
    }

    /**
     * Convenience function.
     *
     * @return
     */
    public Time advance() {
        return new Time(this, 1);
    }

    /**
     *
     * @param o
     * @return  less than 0 if this time is earlier, 0 if this time is equal and larger than 0 if this time is later than o
     */
    @Override
    public int compareTo(@NotNull Time o) {
        return Integer.compare(ticks, o.ticks);
    }

    @Override
    public int hashCode() {
        return ticks;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Time)) {
            return false;
        }
        Time o = (Time) obj;
        return compareTo(o) == 0;
    }

    @Override
    public String toString() {
        return String.valueOf(ticks);
    }
}
