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

/*
 *
 */
package freerails.network.command;

import java.io.Serial;

/**
 * Tells the server to check the filesystem for changes to the available new
 * maps and saved games.
 */
public class RefreshListOfGamesCommandToServer implements CommandToServer {

    @Serial
    private static final long serialVersionUID = -8745171955732354168L;

    /**
     * Creates a request to refresh the server's available-game lists.
     */
    public RefreshListOfGamesCommandToServer() {
        super();
    }

    /**
     * Refreshes the server's saved-game list.
     *
     * @param server server whose game list is refreshed
     * @return successful status
     */
    @Override
    public CommandStatus execute(ServerControlInterface server) {
        server.refreshSavedGames();
        return new CommandStatus(true);
    }

    /**
     * Returns the constant hash code for this stateless command.
     *
     * @return a constant hash code
     */
    @Override
    public int hashCode() {
        return 31;
    }

    /**
     * Compares this stateless command with another instance of the same class.
     *
     * @param obj object to compare
     * @return {@code true} if {@code obj} is a refresh command
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        return getClass() == obj.getClass();
    }

}
