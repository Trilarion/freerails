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

package freerails.client.launcher;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Application services requested by the launcher without exposing server types.
 */
public interface LauncherServices {

    /**
     * Starts listening for clients on the selected port.
     *
     * @param port the port to listen on
     * @return the port actually bound, which may differ when {@code port} is zero
     * @throws IOException if the server cannot listen on the port
     */
    int startHosting(int port) throws IOException;

    /**
     * Loads a saved game into the local server.
     *
     * @param saveGameName the saved game to load
     * @throws IOException if the saved game cannot be loaded
     */
    void loadGame(String saveGameName) throws IOException;

    /**
     * Starts a scenario directly on the local server when no client is running.
     *
     * @param scenarioName the scenario to start
     */
    void startServerGame(String scenarioName);

    /**
     * Returns names of players connected to the local server.
     *
     * @return connected player names
     */
    String[] getPlayerNames();

    /**
     * Registers for connected-player updates.
     *
     * @param listener receives the current player names after each update
     */
    void addConnectedPlayersListener(Consumer<String[]> listener);
}
