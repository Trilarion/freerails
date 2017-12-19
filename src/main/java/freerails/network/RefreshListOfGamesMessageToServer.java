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
 * Created on 24 Jul 2006
 *
 */
package freerails.network;

import freerails.controller.MessageStatus;
import freerails.controller.MessageToServer;
import freerails.controller.ServerControlInterface;

/**
 * Tells the server to check the filesystem for changes to the available new
 * maps and saved games.
 */
public class RefreshListOfGamesMessageToServer implements MessageToServer {

    private static final long serialVersionUID = -8745171955732354168L;
    private final int id;

    /**
     * @param id
     */
    public RefreshListOfGamesMessageToServer(final int id) {
        super();
        this.id = id;
    }

    /**
     * @param server
     * @return
     */
    public MessageStatus execute(ServerControlInterface server) {
        server.refreshSavedGames();
        return new MessageStatus(id, true);
    }

    /**
     * @return
     */
    public int getID() {
        return id;
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final RefreshListOfGamesMessageToServer other = (RefreshListOfGamesMessageToServer) obj;
        return id == other.id;
    }

}
