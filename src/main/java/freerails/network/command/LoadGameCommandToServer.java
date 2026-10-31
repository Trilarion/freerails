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

import java.io.Serial;

/**
 * Request to load a game.
 */
public class LoadGameCommandToServer implements CommandToServer {

    @Serial
    private static final long serialVersionUID = 3256726186552930869L;
    private final String filename;

    /**
     * Creates a request to load the named saved game.
     *
     * @param filename saved-game name or path understood by the server
     */
    public LoadGameCommandToServer(String filename) {
        this.filename = filename;
    }

    /**
     * Compares this request with another by filename.
     *
     * @param obj object to compare
     * @return {@code true} if the other request has the same filename
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof LoadGameCommandToServer loadGameMessageToServer)) return false;

        return filename.equals(loadGameMessageToServer.filename);
    }

    /**
     * Returns a hash code based on the filename.
     *
     * @return this request's hash code
     */
    @Override
    public int hashCode() {
        return filename.hashCode();
    }

    /**
     * Asks the server to load the requested saved game.
     *
     * @param server server that performs the load
     * @return successful status, or a failed status containing the error message
     */
    @Override
    public CommandStatus execute(ServerControlInterface server) {
        try {
            server.loadGame(filename);

            return new CommandStatus(true);
        } catch (Exception e) {
            return new CommandStatus(false, e.getMessage());
        }
    }
}