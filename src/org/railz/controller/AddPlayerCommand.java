/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.railz.controller;

import org.railz.world.player.Player;


/**
 * Sent by the client to request that a player be added to the world
 *
 * TODO server specific password
 */
public class AddPlayerCommand extends ServerCommand {
    private Player player;
    private byte[] signature;

    /**
     * @param signature signature of the Player object with the client's
     * private key
     */
    public AddPlayerCommand(Player p, byte[] signature) {
        player = p;
        this.signature = signature;
    }

    public Player getPlayer() {
        return player;
    }

    public byte[] getSignature() {
        return signature;
    }
}
