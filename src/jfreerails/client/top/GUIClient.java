package jfreerails.client.top;

import java.awt.DisplayMode;
import java.io.IOException;
import java.net.InetAddress;
import java.security.GeneralSecurityException;
import javax.swing.JFrame;
import jfreerails.client.common.ScreenHandler;
import jfreerails.client.common.SynchronizedEventQueue;
import jfreerails.client.view.ModelRoot;
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
    private ScreenHandler screenHandler;
    private String title;
    private ModelRoot modelRoot;

    private GUIClient(ConnectionToServer server, int mode, DisplayMode dm,
        String title, FreerailsProgressMonitor pm, Player player, ModelRoot mr)
        throws IOException, GeneralSecurityException {
        super(player);
        setMoveChainFork(new MoveChainFork());
        setReceiver(new ConnectionAdapter(mr, player, pm, this));
        modelRoot = mr;
        this.title = title;
        SynchronizedEventQueue.use();
        getReceiver().setMoveReceiver(getMoveChainFork());

        modelRoot.setMoveReceiver(getReceiver());
        modelRoot.setMoveFork(getMoveChainFork());

        GUIComponentFactoryImpl gUIComponentFactory = new GUIComponentFactoryImpl(modelRoot);

        modelRoot.addModelRootListener(gUIComponentFactory);

        JFrame client = gUIComponentFactory.createClientJFrame(title);

        //We want to setup the screen handler before creating the view lists
        //since the ViewListsImpl creates images that are compatible with
        //the current display settings and the screen handler may change the
        //display settings.
        screenHandler = new ScreenHandler(client, mode, dm);

        try {
            /* this causes the world to be loaded and the ViewLists to be
             * initialised */
            getReceiver().setConnection(server);

            //Show the frame and set full screen mode if necessary.
            screenHandler.apply();
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

    public ScreenHandler getScreenHandler() {
        return screenHandler;
    }

    public String getTitle() {
        return title;
    }
}