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

package jfreerails.controller;

public interface ConnectionListener {
    /**
     * Indicates that the specified connection was opened by the remote side
     */
    public void connectionOpened(ConnectionToServer c);

    /**
     * Indicates that the specified connection was closed by the remote side
     */
    public void connectionClosed(ConnectionToServer c);

    /**
     * Indicates that the state or number of players of the connection has
     * changed.
     */
    public void connectionStateChanged(ConnectionToServer c);

    /**
     * process a ServerCommand sent by the remote side
     */
    public void processServerCommand(ConnectionToServer c, ServerCommand s);
}