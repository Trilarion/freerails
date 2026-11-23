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

import java.io.Serializable;

/**
 * Defines a command sent from the server to the client.
 *
 * <p>Commands control the session, for example updating the lists of connected
 * clients, scenarios, and saved games, or replacing the client's whole world.
 * They are deliberately not moves: a {@link freerails.move.Move} is a checked
 * change to an existing world that clients can pre-commit and undo, whereas a
 * command may act on state outside the world or replace the world itself.
 *
 * <p>The client replies to each command with the {@link CommandStatus} returned
 * by {@link #execute(ClientControlInterface)}, so that the server can know
 * whether the command failed on the client.
 *
 * @see CommandToServer
 */
public interface CommandToClient extends Serializable {
    /**
     * Executes this command on a client.
     *
     * @param client client control interface receiving the command
     * @return status describing whether execution succeeded
     */
    CommandStatus execute(ClientControlInterface client);

}