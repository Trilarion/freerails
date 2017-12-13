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

import java.awt.DisplayMode;
import java.io.IOException;
import java.net.InetAddress;
import java.security.GeneralSecurityException;
import javax.swing.JFrame;
import jfreerails.client.common.ScreenHandler;
import jfreerails.client.common.SynchronizedEventQueue;
import jfreerails.client.common.UpdatedComponent;
import jfreerails.client.view.GUIRoot;
import jfreerails.client.model.ModelRoot;
import jfreerails.controller.ConnectionToServer;
import jfreerails.controller.InetConnection;
import jfreerails.controller.LocalConnection;
import jfreerails.controller.MoveChainFork;
import jfreerails.controller.ServerControlInterface;
import jfreerails.util.FreerailsProgressMonitor;
import jfreerails.world.player.Player;


/**
 * This class implements a GUI-driven client to be used by human players.
 *
 * XXX How should the server be controlled from the client? (loading, saving of
 * maps etc?). Currently we will do this over the local connection only, by
 * the client having access to a ServerControlInterface object
 */
public class GUIClient extends Client {
    private String title;
    private ModelRoot modelRoot;
    private GUIRoot guiRoot;

    private GUIClient(ConnectionToServer server, int mode, DisplayMode dm,
        String title, FreerailsProgressMonitor pm, Player player, ModelRoot mr)
        throws IOException, GeneralSecurityException {
        super(player);
        setMoveChainFork(new MoveChainFork());
        modelRoot = mr;
        this.title = title;
        SynchronizedEventQueue.use();
	
        modelRoot.setMoveFork(getMoveChainFork());

	/* create the GUIRoot */
	guiRoot = new
	    GUIRoot(modelRoot);

	setReceiver(new ConnectionAdapter(mr, guiRoot, player, pm,
		    this));
        modelRoot.setMoveReceiver(getReceiver());
        getReceiver().setMoveReceiver(getMoveChainFork());

        //We want to setup the screen handler before creating the view lists
        //since the ViewListsImpl creates images that are compatible with
        //the current display settings and the screen handler may change the
        //display settings.
	guiRoot.setScreenHandler(new
		ScreenHandler(guiRoot.getClientJFrame(),
		    (UpdatedComponent) guiRoot.getClientJFrame(),
		    mode, dm));

        try {
            /* this causes the world to be loaded and the ViewLists to be
             * initialised */
            getReceiver().setConnection(server);
        } catch (GeneralSecurityException e) {
            server.close();
            throw e;
        }
    }

    /**
     * Start a client with an internet connection to a server
     */
    public GUIClient(InetAddress server, int mode, DisplayMode dm,
        String title, FreerailsProgressMonitor pm, Player player)
        throws IOException, GeneralSecurityException {
        this(new InetConnection(server), mode, dm, title, pm, player,
            new ModelRoot());
    }

    /**
     * sets up a connnection with a local server. Currently this is the only
     * form of connection supported.
     * @throws java.io.IOException if the connection could not be opened
     */
    public GUIClient(ServerControlInterface controls, LocalConnection server,
        int mode, DisplayMode dm, String title, FreerailsProgressMonitor pm,
        Player player) throws IOException, GeneralSecurityException {
        this((ConnectionToServer)new LocalConnection(server), mode, dm, title,
            pm, player, new ModelRoot());
        modelRoot.setServerControls(controls);
    }

    public String getTitle() {
        return title;
    }

    public GUIRoot getGUIRoot() {
	return guiRoot;
    }
}
