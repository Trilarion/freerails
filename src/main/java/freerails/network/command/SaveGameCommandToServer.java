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
    private final int id;
    private final String filename;

    /**
     * @param id
     * @param s
     */
    public SaveGameCommandToServer(int id, String s) {
        this.id = id;
        filename = s;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SaveGameCommandToServer)) return false;

        final SaveGameCommandToServer saveGameMessageToServer = (SaveGameCommandToServer) obj;

        if (id != saveGameMessageToServer.id) return false;
        return filename.equals(saveGameMessageToServer.filename);
    }

    @Override
    public int hashCode() {
        int result;
        result = id;
        result = 29 * result + filename.hashCode();
        return result;
    }

    /**
     * @param server
     * @return
     */
    public CommandStatus execute(ServerControlInterface server) {
        try {
            server.saveGame(filename);
            return new CommandStatus(id, true);
        } catch (Exception e) {
            return new CommandStatus(id, false, e.getMessage());
        }
    }
}