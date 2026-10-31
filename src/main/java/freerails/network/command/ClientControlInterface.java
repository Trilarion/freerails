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

import java.io.Serializable;

/**
 * Defines the methods that the server can call on a client using a
 * CommandToClient.
 *
 * @see CommandToClient
 */
public interface ClientControlInterface {

    /**
     * Replaces the client's game model after a new game starts or is loaded.
     *
     * @param world game world to install
     */
    void setGameModel(World world);

    /**
     * Updates a client-side property, such as the list of saved games.
     *
     * @param propertyName property to update
     * @param value value assigned to the property
     */
    void setProperty(ClientProperty propertyName, Serializable value);

}