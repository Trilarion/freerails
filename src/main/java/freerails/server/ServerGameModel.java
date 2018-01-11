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

/*
 *
 */
package freerails.server;

import freerails.network.MoveReceiver;
import freerails.world.World;
import freerails.world.game.GameModel;

import java.io.Serializable;

/**
 * Defines methods on a GameModel that let the server load and initiate, and
 * save it.
 */
public interface ServerGameModel extends GameModel, Serializable {

    /**
     * @param world
     * @param passwords
     */
    void setWorld(World world, String[] passwords);

    /**
     * @return
     */
    World getWorld();

    /**
     * @return
     */
    String[] getPasswords();

    /**
     * @param moveReceiver
     */
    void initialize(MoveReceiver moveReceiver);

}