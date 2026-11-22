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

package freerails.server;

import freerails.model.world.World;
import freerails.move.MoveReceiver;

import java.io.Serializable;

/**
 * Defines the server-side game model that the server loads, initializes,
 * updates, and saves.
 */
public interface ServerGameModel extends Serializable {

    /**
     * Advances the model by one server update cycle.
     *
     * @param moveReceiver the receiver to which generated moves are submitted
     */
    void update(MoveReceiver moveReceiver);

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

}