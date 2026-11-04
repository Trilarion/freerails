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

package freerails.network.command;

import freerails.model.world.World;
import freerails.util.Utils;

import java.io.Serial;

/**
 * Sent from the server to the client when (i) a new game is started, (ii) a
 * game is loaded, or (iii) the client connects to a game in progress.
 */
public class SetWorldCommandToClient implements CommandToClient {

    @Serial
    private static final long serialVersionUID = 3257570619972269362L;
    private final World world;

    // TODO why a defensive copy (actually some things do not work otherwise), maybe because in local connection, nothing is serialized
    /**
     * Creates a command containing a defensive copy of the supplied world.
     *
     * @param world world to copy for delivery to the client
     */
    public SetWorldCommandToClient(World world) {
        this.world = (World) Utils.cloneBySerialisation(world);
    }

    /**
     * Installs a defensive copy of the world on the client.
     *
     * @param client client receiving the world
     * @return successful status after the world is installed
     */
    @Override
    public CommandStatus execute(ClientControlInterface client) {
        client.setGameModel((World) Utils.cloneBySerialisation(world));
        return new CommandStatus(true);
    }

    /**
     * Compares this command with another by world contents.
     *
     * @param obj object to compare
     * @return {@code true} if the other command contains an equal world
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SetWorldCommandToClient setWorldMessageToClient)) return false;

        return world.equals(setWorldMessageToClient.world);
    }

    /**
     * Returns a hash code based on the world.
     *
     * @return this command's hash code
     */
    @Override
    public int hashCode() {
        return world.hashCode();
    }
}