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
package freerails.client;

import freerails.move.MoveExecutor;
import freerails.network.command.CommandToServer;

/**
 * Gives GUI classes access to a loaded game: the world, the local player, move execution and the
 * client properties. An instance only exists once a world and a player are known.
 */
public interface ModelRoot extends MoveExecutor {

    /**
     * Performs the send command operation.
     *
     * @param message the message parameter
     */
    void sendCommand(CommandToServer message);

    /**
     * Returns the client-side GUI properties, which outlive this game session.
     *
     * @return the client properties
     */
    ClientProperties getProperties();

}