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
package freerails.world.game;

import java.io.Serializable;

/**
 * Represents a specific instant in time during a game.
 */
public class GameTime implements Serializable, Comparable<GameTime> {
    /**
     * The first possible time.
     */
    public static final GameTime BIG_BANG = new GameTime(Integer.MIN_VALUE);
    /**
     * The last possible time.
     */
    public static final GameTime DOOMSDAY = new GameTime(Integer.MAX_VALUE);
    private static final long serialVersionUID = 3691035461301055541L;
    private final int ticks;

    /**
     * @param ticks
     */
    public GameTime(int ticks) {
        this.ticks = ticks;
    }

    @Override
    public String toString() {
        return "GameTime:" + String.valueOf(ticks);
    }

    @Override
    public int hashCode() {
        return ticks;
    }

    /**
     * @return
     */
    public GameTime advancedTime() {
        return new GameTime(ticks + 1);
    }

    /**
     * @return
     */
    public int getTicks() {
        return ticks;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GameTime) {
            GameTime test = (GameTime) obj;

            return ticks == test.ticks;
        }
        return false;
    }

    /**
     * Compares two GameTimes for ordering.
     *
     * @return 0 if t is equal to this GameTime; a value less than 0 if this
     * GameTime is before t; and a value greater than 0 if this GameTime
     * is after t.
     */
    public int compareTo(GameTime o) {
        return ticks - o.ticks;
    }

}