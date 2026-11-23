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
 * Defines a command sent from a client to the server.
 *
 * <p>Commands control the session, for example starting, loading, or saving a
 * game. They are deliberately not moves: changes to the game world are sent as
 * {@link freerails.move.Move}s, which the server checks and broadcasts.
 *
 * @see CommandToClient
 */
public interface CommandToServer extends Serializable {

    /**
     * Executes this command on the server.
     *
     * @param server server control interface receiving the command
     * @return status describing whether execution succeeded
     */
    CommandStatus execute(ServerControlInterface server);
}