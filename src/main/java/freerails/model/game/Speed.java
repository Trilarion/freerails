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

import java.io.Serializable;

/**
 *
 */
public enum Speed implements Serializable {
    PAUSE(0), SLOW(10), MODERATE(20), FAST(30);

    private final int ticksPerSecond;

    Speed(int ticksPerSecond) {
        if (ticksPerSecond < 0) {
            throw new IllegalArgumentException();
        }
        this.ticksPerSecond = ticksPerSecond;
    }

    public int getTicksPerSecond() {
        return ticksPerSecond;
    }
}
