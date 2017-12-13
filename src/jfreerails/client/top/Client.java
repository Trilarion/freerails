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

package jfreerails.client.top;

import jfreerails.controller.MoveChainFork;
import jfreerails.world.player.Player;


/**
 * Represents an instance of a jfreerails client. It provides access to common
 * services which implementations make use of. Objects within the client
 * keep a reference to an instance of this object to access per-client objects.
 */
public abstract class Client {
    private MoveChainFork moveChainFork;
    private ConnectionAdapter receiver;

    protected Client(Player p) {
    }

    public ConnectionAdapter getReceiver() {
        return receiver;
    }

    protected void setMoveChainFork(MoveChainFork moveChainFork) {
        this.moveChainFork = moveChainFork;
    }

    /**
     * @return  A MoveChainFork to which classes may subscribe to receive Moves
     */
    protected MoveChainFork getMoveChainFork() {
        return moveChainFork;
    }

    protected void setReceiver(ConnectionAdapter receiver) {
        this.receiver = receiver;
    }
}