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

import freerails.util.Utils;

import java.io.Serializable;

/**
 * Provides a set of keys to access the lists of elements in the game
 * world that are shared by all players.
 */
public enum WorldSharedKey {

    TerrainTypes(0), WagonTypes(1), CargoTypes(2), Cities(3), EngineTypes(4), TrackRules(5);

    private final int id;

    WorldSharedKey(int id) {
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

    public static WorldSharedKey getById(int id) {
        for (WorldSharedKey key : values()) {
            if (key.id == id) return key;
        }
        throw new IllegalArgumentException();
    }
}