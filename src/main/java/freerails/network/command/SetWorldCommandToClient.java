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
import freerails.util.Utils;

/**
 * Sent from the server to the client when (i) a new game is started, (ii) a
 * game is loaded, or (iii) the client connects to a game in progress.
 */
public class SetWorldCommandToClient implements CommandToClient {

    private static final long serialVersionUID = 3257570619972269362L;
    private final World world;

    // TODO why a defensive copy (actually some things do not work otherwise), maybe because in local connection, nothing is serialized
    /**
     * Note, makes a defensive copy of the world object passed to it.
     */
    public SetWorldCommandToClient(World world) {
        /**
         * Returns a copy of this world object - making changes to this copy will
         * not change this object.
         */
        this.world = (World) Utils.cloneBySerialisation(world);
        // this.world = world;
    }

    public CommandStatus execute(ClientControlInterface client) {
        /**
         * Returns a copy of this world object - making changes to this copy will
         * not change this object.
         */
        client.setGameModel((World) Utils.cloneBySerialisation(world));

        return new CommandStatus(true);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SetWorldCommandToClient)) return false;

        final SetWorldCommandToClient setWorldMessageToClient = (SetWorldCommandToClient) obj;

        return world.equals(setWorldMessageToClient.world);
    }

    @Override
    public int hashCode() {
        return world.hashCode();
    }
}