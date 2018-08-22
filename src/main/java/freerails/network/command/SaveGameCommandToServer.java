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
 * A request to save the game.
 */
public class SaveGameCommandToServer implements CommandToServer {

    private static final long serialVersionUID = 3257281452725777209L;
    private final String filename;

    /**
     * @param s
     */
    public SaveGameCommandToServer(String s) {
        filename = s;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SaveGameCommandToServer)) return false;

        final SaveGameCommandToServer saveGameMessageToServer = (SaveGameCommandToServer) obj;

        return filename.equals(saveGameMessageToServer.filename);
    }

    @Override
    public int hashCode() {
        return filename.hashCode();
    }

    /**
     * @param server
     * @return
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