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
 * A request to save the game.
 */
public class SaveGameCommandToServer implements CommandToServer {

    @Serial
    private static final long serialVersionUID = 3257281452725777209L;
    private final String filename;

    /**
     * Creates a request to save the current game under the given name.
     *
     * @param s saved-game name or path understood by the server
     */
    public SaveGameCommandToServer(String s) {
        filename = s;
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
        if (!(obj instanceof SaveGameCommandToServer saveGameMessageToServer)) return false;

        return filename.equals(saveGameMessageToServer.filename);
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
     * Asks the server to save the current game.
     *
     * @param server server that performs the save
     * @return successful status, or a failed status containing the error message
     */
    @Override
    public CommandStatus execute(ServerControlInterface server) {
        try {
            server.saveGame(filename);
            return new CommandStatus(true);
        } catch (Exception e) {
            return new CommandStatus(false, e.getMessage());
        }
    }
}