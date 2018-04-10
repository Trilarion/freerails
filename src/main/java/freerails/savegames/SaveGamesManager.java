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

package freerails.savegames;

import freerails.server.ServerGameModel;

import java.io.IOException;
import java.io.Serializable;

// TODO move newMap out of SaveGamesManager, not the task!
/**
 * Defines methods that let the server load and save game states, and get blank
 * maps for new games.
 */
public interface SaveGamesManager {

    /**
     * @return
     */
    String[] getSaveGameNames();

    /**
     * @param path
     * @param serializable
     * @throws IOException
     */
    void saveGame(String path, Serializable serializable) throws IOException;

    /**
     * @param path
     * @return
     * @throws IOException
     */
    ServerGameModel loadGame(String path) throws IOException;
}