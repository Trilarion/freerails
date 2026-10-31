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

package freerails.scenario;

import freerails.server.ServerGameModel;
import freerails.util.Utils;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Stores saved games in memory rather than on disk.
 */
public class TestSaveGamesManager implements SaveGamesManager {

    private final Map<String, Serializable> savedGames = new HashMap<>();

    /**
     * Returns the save game names.
     * @return the save game names.
     */
    @Override
    public String[] getSaveGameNames() {
        return savedGames.keySet().toArray(new String[0]);
    }

    /**
     * Performs save game.
     * @param path the path to use in the test.
     * @param serializable the serializable to use in the test.
     */
    @Override
    public void saveGame(String path, Serializable serializable) {
        // Make a copy so that the saved version's state cannot be changed.
        Serializable copy = Utils.cloneBySerialisation(serializable);
        savedGames.put(path, copy);
    }

    /**
     * Performs load game.
     * @param path the path to use in the test.
     * @return the result of load game.
     */
    @Override
    public ServerGameModel loadGame(String path) {
        Serializable o = savedGames.get(path);
        return (ServerGameModel) Utils.cloneBySerialisation(o);
    }
}