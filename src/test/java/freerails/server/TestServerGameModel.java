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

import freerails.model.world.World;

// TODO not sure this is really needed, just doesn't do anything
/**
 * A ServerGameModel that has a world object but no automation.
 */
public class TestServerGameModel implements ServerGameModel {

    private static final long serialVersionUID = 3546074757457131826L;
    private World world;
    private String[] passwords;

    /**
     * @param world
     * @param passwords
     */
    @Override
    public void setWorld(World world, String[] passwords) {
        this.world = world;
        this.passwords = passwords.clone();
    }

    /**
     * @return
     */
    @Override
    public World getWorld() {
        return world;
    }

    /**
     * @param moveReceiver
     */
    @Override
    public void initialize(MoveReceiver moveReceiver) {}

    /**
     *
     */
    @Override
    public void update() {}

    /**
     * @return
     */
    @Override
    public String[] getPasswords() {
        return passwords.clone();
    }

}