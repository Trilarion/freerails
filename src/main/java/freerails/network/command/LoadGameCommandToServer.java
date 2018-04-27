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

package freerails.network.command;

/**
 * Request to load a game.
 */
public class LoadGameCommandToServer implements CommandToServer {

    private static final long serialVersionUID = 3256726186552930869L;
    private final String filename;

    /**
     * @param filename
     */
    public LoadGameCommandToServer(String filename) {
        this.filename = filename;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof LoadGameCommandToServer)) return false;

        final LoadGameCommandToServer loadGameMessageToServer = (LoadGameCommandToServer) obj;

        return filename.equals(loadGameMessageToServer.filename);
    }

    @Override
    public int hashCode() {
        return filename.hashCode();
    }

    /**
     * @param server
     * @return
     */
    public CommandStatus execute(ServerControlInterface server) {
        try {
            server.loadGame(filename);

            return new CommandStatus(true);
        } catch (Exception e) {
            return new CommandStatus(false, e.getMessage());
        }
    }
}