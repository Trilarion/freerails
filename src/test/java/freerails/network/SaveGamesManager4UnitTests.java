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

package freerails.network;

import freerails.util.Utils;
import freerails.world.WorldImpl;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

/**
 * Stores saved games in memory rather than on disk.
 */
public class SaveGamesManager4UnitTests implements SaveGamesManager {
    private final String[] mapsAvailable = {"map1", "map2"};

    private final HashMap<String, Serializable> savedGames = new HashMap<>();

    /**
     * @return
     */
    public String[] getSaveGameNames() {
        Object[] keys = savedGames.keySet().toArray();

        String[] names = new String[keys.length];

        for (int i = 0; i < names.length; i++) {
            names[i] = (String) keys[i];
        }

        return names;
    }

    /**
     * @return
     */
    public String[] getNewMapNames() {
        return mapsAvailable.clone();
    }

    /**
     * @param w
     * @param s
     * @throws IOException
     */
    public void saveGame(Serializable w, String s) throws IOException {
        // Make a copy so that the saved version's state cannot be changed.
        Serializable copy = Utils.cloneBySerialisation(w);
        this.savedGames.put(s, copy);
    }

    /**
     * @param name
     * @return
     * @throws IOException
     */
    public Serializable loadGame(String name) throws IOException {
        Serializable o = savedGames.get(name);

        return Utils.cloneBySerialisation(o);
    }

    /**
     * @param name
     * @return
     * @throws IOException
     */
    public Serializable newMap(String name) throws IOException {
        return new WorldImpl(10, 10);
    }
}