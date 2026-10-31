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
package freerails.server;

import freerails.model.world.World;

import java.io.Serializable;

// TODO do not extend GameModel, just add an update method
/**
 * Defines methods on a GameModel that let the server load and initiate, and
 * save it.
 */
public interface ServerGameModel extends GameModel, Serializable {

    /**
     * Replaces the model's world and associated player passwords.
     *
     * @param world the game world used by this operation
     * @param passwords the player passwords associated with the world
     */
    void setWorld(World world, String[] passwords);

    /**
     * Returns the world maintained by this server model.
     *
     * @return the current game world
     */
    World getWorld();

    /**
     * Returns the passwords associated with the players in the world.
     *
     * @return the player passwords
     */
    String[] getPasswords();

    /**
     * Initializes server processing and sends generated moves to the receiver.
     *
     * @param moveReceiver the receiver to which generated moves are submitted
     */
    void initialize(MoveReceiver moveReceiver);

}