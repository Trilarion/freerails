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
 * Provides a set of keys to access the lists of elements in the game world that are indexed by player.
 */
public enum PlayerKey {

    Trains(0), Stations(1), CargoBundles(2), TrainSchedules(3);

    private final int id;

    PlayerKey(int id) {
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

    public static PlayerKey getById(int id) {
        for (PlayerKey key : values()) {
            if (key.id == id) return key;
        }
        throw new IllegalArgumentException();
    }
}
