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

package freerails.network;

import freerails.controller.MessageStatus;
import freerails.controller.ServerControlInterface;

/**
 * Request to load a game.
 */
public class LoadGameMessageToServer implements MessageToServer {

    private static final long serialVersionUID = 3256726186552930869L;
    private final int id;
    private final String filename;

    /**
     * @param id
     * @param s
     */
    public LoadGameMessageToServer(int id, String s) {
        this.id = id;
        filename = s;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof LoadGameMessageToServer)) return false;

        final LoadGameMessageToServer loadGameMessageToServer = (LoadGameMessageToServer) obj;

        if (id != loadGameMessageToServer.id) return false;
        return filename.equals(loadGameMessageToServer.filename);
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
    public MessageStatus execute(ServerControlInterface server) {
        try {
            server.loadgame(filename);

            return new MessageStatus(id, true);
        } catch (Exception e) {

            return new MessageStatus(id, false, e.getMessage());
        }
    }
}