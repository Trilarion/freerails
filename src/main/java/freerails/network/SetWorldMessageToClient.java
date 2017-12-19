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

import freerails.controller.ClientControlInterface;
import freerails.controller.MessageStatus;
import freerails.controller.MessageToClient;
import freerails.util.Immutable;
import freerails.world.top.World;

/**
 * Sent from the server to the client when (i) a new game is started, (ii) a
 * game is loaded, or (iii) the client connects to a game in progress.
 */
@Immutable
public class SetWorldMessageToClient implements MessageToClient {
    private static final long serialVersionUID = 3257570619972269362L;

    private final int id;

    private final World world;

    /**
     * Note, makes a defensive copy of the world object passed to it.
     *
     * @param id
     * @param world
     */
    public SetWorldMessageToClient(int id, World world) {
        this.id = id;
        this.world = world.defensiveCopy();
    }

    public MessageStatus execute(ClientControlInterface client) {
        client.setGameModel(world.defensiveCopy());

        return new MessageStatus(id, true);
    }

    public int getID() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SetWorldMessageToClient))
            return false;

        final SetWorldMessageToClient setWorldMessageToClient = (SetWorldMessageToClient) o;

        if (id != setWorldMessageToClient.id)
            return false;
        return world.equals(setWorldMessageToClient.world);
    }

    @Override
    public int hashCode() {
        int result;
        result = id;
        result = 29 * result + world.hashCode();
        return result;
    }
}