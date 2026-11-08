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

// TODO the id for all the commands is not really used, do we really need it

import java.io.Serial;

/**
 * Request to start a game on a new map.
 */
public class NewGameCommandToServer implements CommandToServer {

    @Serial
    private static final long serialVersionUID = 3256723961743422513L;
    private final String mapName;

    /**
     * Creates a request to start a game using the named map.
     *
     * @param s map name understood by the server
     */
    public NewGameCommandToServer(String s) {
        mapName = s;
    }

    /**
     * Asks the server to create a game using the requested map.
     *
     * @param server server that creates the game
     * @return successful status, or a failed status
     */
    @Override
    public CommandStatus execute(ServerControlInterface server) {
        try {
            server.newGame(mapName);

            return new CommandStatus(true);
        } catch (Exception e) {
            return new CommandStatus(false);
        }
    }

    /**
     * Compares this request with another by map name.
     *
     * @param obj object to compare
     * @return {@code true} if the other request has the same map name
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NewGameCommandToServer newGameMessageToServer)) return false;

        return mapName.equals(newGameMessageToServer.mapName);
    }

    /**
     * Returns a hash code based on the map name.
     *
     * @return this request's hash code
     */
    @Override
    public int hashCode() {
        return mapName.hashCode();
    }
}