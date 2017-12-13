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

import org.railz.world.player.FreerailsPrincipal;


/**
 * Sent by the server to indicate that a request to add a player to the world
 * was rejected or accepted.
 */
public class AddPlayerResponseCommand extends ServerCommand {
    private boolean rejected;
    private FreerailsPrincipal principal;

    /**
     * The request was accepted
     * @param p the principal that represents the added player
     */
    public AddPlayerResponseCommand(FreerailsPrincipal p) {
        rejected = false;
        principal = p;
    }

    /**
     * The request was rejected
     */
    public AddPlayerResponseCommand(AddPlayerCommand c, String reason) {
        rejected = true;
    }

    public FreerailsPrincipal getPrincipal() {
        return principal;
    }

    public boolean isRejected() {
        return rejected;
    }
}
