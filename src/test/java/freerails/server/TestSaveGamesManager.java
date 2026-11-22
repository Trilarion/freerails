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

package freerails.server;

import freerails.util.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores saved games in memory rather than on disk.
 */
public class TestSaveGamesManager implements SaveGamesManager {

    private final Map<String, SavedGame> savedGames = new HashMap<>();

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
     * @param savedGame the saved game to use in the test.
     */
    @Override
    public void saveGame(String path, SavedGame savedGame) {
        // Make a copy so that the saved version's state cannot be changed.
        savedGames.put(path, (SavedGame) Utils.cloneBySerialisation(savedGame));
    }

    /**
     * Performs load game.
     * @param path the path to use in the test.
     * @return the result of load game.
     */
    @Override
    public SavedGame loadGame(String path) {
        return (SavedGame) Utils.cloneBySerialisation(savedGames.get(path));
    }
}
