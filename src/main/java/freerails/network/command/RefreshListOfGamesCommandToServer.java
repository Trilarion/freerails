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

/*
 *
 */
package freerails.network.command;

/**
 * Tells the server to check the filesystem for changes to the available new
 * maps and saved games.
 */
public class RefreshListOfGamesCommandToServer implements CommandToServer {

    private static final long serialVersionUID = -8745171955732354168L;

    /**
     */
    public RefreshListOfGamesCommandToServer() {
        super();
    }

    /**
     * @param server
     * @return
     */
    @Override
    public CommandStatus execute(ServerControlInterface server) {
        server.refreshSavedGames();
        return new CommandStatus(true);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final RefreshListOfGamesCommandToServer other = (RefreshListOfGamesCommandToServer) obj;
        return true;
    }

}
