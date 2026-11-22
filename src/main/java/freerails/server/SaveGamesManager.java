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

import java.io.IOException;

/**
 * Defines methods that let the server list, load, and save game states.
 */
public interface SaveGamesManager {

    /**
     * Returns the names of saved games available to load.
     *
     * @return the saved-game names
     */
    String[] getSaveGameNames();

    /**
     * Writes a saved game to a named save-game file.
     *
     * @param path the path of the save-game file
     * @param savedGame the world and player credentials to save
     * @throws IOException if the operation cannot complete because of an input/output error
     */
    void saveGame(String path, SavedGame savedGame) throws IOException;

    /**
     * Loads a saved game from a save-game file.
     *
     * @param path the path of the save-game file
     * @return the loaded world and player credentials
     * @throws IOException if the operation cannot complete because of an input/output error
     */
    SavedGame loadGame(String path) throws IOException;
}
