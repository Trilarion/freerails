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

package freerails.server;

import freerails.network.MoveReceiver;

import java.io.Serializable;

/**
 * This interface is implemented by objects which are responsible for updating
 * the game world. They are serialized when the game is saved. They are internal
 * clients of the ServerGameEngine and need to be initialised with a connection
 * to the game when deserialized.
 */
// TODO where are they really used?
interface ServerAutomaton extends Serializable {

    /**
     * Initializes the automaton with a connection to the MoveExecutor.
     */
    void initAutomaton(MoveReceiver moveReceiver);
}