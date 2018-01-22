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
 * Request to start a game on a new map.
 */
public class NewGameMessageToServer implements MessageToServer {

    private static final long serialVersionUID = 3256723961743422513L;
    private final int id;
    private final String mapName;

    /**
     * @param id
     * @param s
     */
    public NewGameMessageToServer(int id, String s) {
        this.id = id;
        mapName = s;
    }

    // TODO This would be better implemented in a config file, or better still dynamically determined by scanning the directory.
    public static String[] getMapNames() {
        return new String[]{"South America", "Small South America"};
    }

    /**
     * @param server
     * @return
     */
    public MessageStatus execute(ServerControlInterface server) {
        try {
            server.newGame(mapName);

            return new MessageStatus(id, true);
        } catch (Exception e) {
            return new MessageStatus(id, false, e.getMessage());
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NewGameMessageToServer)) return false;

        final NewGameMessageToServer newGameMessageToServer = (NewGameMessageToServer) obj;

        if (id != newGameMessageToServer.id) return false;
        return mapName.equals(newGameMessageToServer.mapName);
    }

    @Override
    public int hashCode() {
        int result;
        result = id;
        result = 29 * result + mapName.hashCode();
        return result;
    }
}