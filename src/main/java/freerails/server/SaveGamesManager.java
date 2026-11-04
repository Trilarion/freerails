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

import freerails.server.ServerGameModel;

import java.io.IOException;
import java.io.Serializable;

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
     * Serializes a game object to a named save-game file.
     *
     * @param path the path of the save-game file
     * @param serializable the object to serialize to the save-game file
     * @throws IOException if the operation cannot complete because of an input/output error
     */
    void saveGame(String path, Serializable serializable) throws IOException;

    /**
     * Loads a server game model from a save-game file.
     *
     * @param path the path of the save-game file
     * @return the loaded server game model
     * @throws IOException if the operation cannot complete because of an input/output error
     */
    ServerGameModel loadGame(String path) throws IOException;
}