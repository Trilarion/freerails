/*
 * Created on Feb 18, 2004
 */
package jfreerails.server;

import javax.swing.table.DefaultTableModel;
import jfreerails.controller.ConnectionToServer;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.player.Player;


/**
 * Table model which represents currently connected clients.
 * Connection states are described as follows:
 * <ol>
 * @author rob
 *  @author Luke
 */
class ClientConnectionTableModel extends DefaultTableModel {
    private final ServerGameController gameController;

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
        setValueAt(c.getConnectionState().toString(), i, 1);
        setValueAt(getPlayerName(c), i, 2);
    }

    public synchronized void removeRow(int i) {
        super.removeRow(i);
    }

    public boolean isCellEditable(int r, int c) {
        return false;
    }
}