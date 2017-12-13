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

package org.railz.server;


/**
 * This interface is implemented by objects which are responsible for updating
 * the game world. They are serialized when the game is saved.
 * They are internal clients of the ServerGameEngine and need to be initialised
 * with a connection to the game when deserialized.
 */
import org.railz.world.common.FreerailsSerializable;
import org.railz.controller.MoveReceiver;


public interface ServerAutomaton extends FreerailsSerializable {
    /**
     * Initializes the automaton with a connection to the MoveExecuter
     */
    public void initAutomaton(MoveReceiver mr);
}
