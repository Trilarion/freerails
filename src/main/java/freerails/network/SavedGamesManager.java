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

import java.io.IOException;
import java.io.Serializable;

/**
 * Defines methods that let the server load and save game states, and get blank
 * maps for new games.
 *
 */
public interface SavedGamesManager {

    /**
     *
     * @return
     */
    String[] getSaveGameNames();

    /**
     *
     * @return
     */
    String[] getNewMapNames();

    /**
     *
     * @param w
     * @param s
     * @throws IOException
     */
    void saveGame(Serializable w, String s) throws IOException;

    /**
     *
     * @param name
     * @return
     * @throws IOException
     */
    Serializable loadGame(String name) throws IOException;

    /**
     *
     * @param name
     * @return
     * @throws IOException
     */
    Serializable newMap(String name) throws IOException;
}