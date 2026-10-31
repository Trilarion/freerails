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

package freerails.network.command;

import java.io.IOException;

/**
 * Defines the methods that a client can call on the server using a CommandToServer.
 *
 * @see CommandToServer
 */
public interface ServerControlInterface {

    /**
     * Loads a saved game and makes it the server's current game.
     *
     * @param saveGameName saved-game name or path
     * @throws IOException if the saved game cannot be read
     */
    void loadGame(String saveGameName) throws IOException;

    /**
     * Saves the current game using the supplied name.
     *
     * @param saveGameName destination saved-game name
     */
    void saveGame(String saveGameName);

    /**
     * Refreshes the list of saved games available from the server.
     */
    void refreshSavedGames();

    /**
     * Starts a new game using the named map.
     *
     * @param mapName map name understood by the server
     */
    void newGame(String mapName);
}