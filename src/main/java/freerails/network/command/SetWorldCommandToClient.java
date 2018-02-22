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

import freerails.model.world.World;

/**
 * Sent from the server to the client when (i) a new game is started, (ii) a
 * game is loaded, or (iii) the client connects to a game in progress.
 */
public class SetWorldCommandToClient implements CommandToClient {

    private static final long serialVersionUID = 3257570619972269362L;
    private final int id;
    private final World world;

    // TODO why a defensive copy (actually some things do not work otherwise), maybe because in local connection, nothing is serialized
    /**
     * Note, makes a defensive copy of the world object passed to it.
     */
    public SetWorldCommandToClient(int id, World world) {
        this.id = id;
        this.world = world.defensiveCopy();
        // this.world = world;
    }

    public CommandStatus execute(ClientControlInterface client) {
        client.setGameModel(world.defensiveCopy());

        return new CommandStatus(id, true);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SetWorldCommandToClient)) return false;

        final SetWorldCommandToClient setWorldMessageToClient = (SetWorldCommandToClient) obj;

        if (id != setWorldMessageToClient.id) return false;
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