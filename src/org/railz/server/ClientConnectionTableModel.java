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

/*
 * Created on Feb 18, 2004
 */
package org.railz.server;

import javax.swing.table.DefaultTableModel;
import org.railz.controller.ConnectionToServer;
import org.railz.world.player.FreerailsPrincipal;
import org.railz.world.player.Player;


/**
 * Table model which represents currently connected clients.
 * Connection states are described as follows:
 * <ol>
 */
class ClientConnectionTableModel extends DefaultTableModel {
    private ServerGameController gameController;

    public ClientConnectionTableModel(ServerGameController sgc) {
        super(new String[] {"Client address", "State", "Player"}, 0);
        gameController = sgc;
    }

    private String getPlayerName(ConnectionToServer c) {
        IdentityProvider ip = gameController.gameEngine.getIdentityProvider();
        FreerailsPrincipal p = ip.getPrincipal(c);

        if (p != null) {
            Player pl;

            if ((pl = ip.getPlayer(p)) == null) {
                return p.getName();
            } else {
                return pl.getName();
            }
        } else {
            return "Player not authenticated.";
        }
    }

    synchronized void addRow(ConnectionToServer c, String address) {
        addRow(new String[] {
                address, c.getConnectionState().toString(), getPlayerName(c)
            });
    }

    synchronized void stateChanged(ConnectionToServer c, int i) {
	if (i >= 0) {
	    setValueAt(c.getConnectionState().toString(), i, 1);
	    setValueAt(getPlayerName(c), i, 2);
	}
    }

    public synchronized void removeRow(int i) {
	if (i >= 0) {
	    super.removeRow(i);
	}
    }

    public boolean isCellEditable(int r, int c) {
        return false;
    }
}
