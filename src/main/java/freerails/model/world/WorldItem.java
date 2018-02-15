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

package freerails.model.world;

/**
 * Provides a set of items to access the items of which there can only
 * be one instance in the game world (for example, the current time).
 */
public enum WorldItem {

    Calendar(0), GameRules(1), GameSpeed(2), EconomicClimate(3);

    private final int id;

    WorldItem(int id) {
        this.id = id;
    }

    /**
     * @return
     */
    public static int getNumberOfKeys() {
        return values().length;
    }

    /**
     *
     * @return
     */
    public int getId() {
        return id;
    }

    public static WorldItem getById(int id) {
        for (WorldItem key : values()) {
            if (key.id == id) return key;
        }
        throw new IllegalArgumentException();
    }
}