/*
 * Created on Sep 11, 2004
 *
 */
package jfreerails.launcher;

import java.awt.DisplayMode;
import java.io.IOException;
import javax.swing.JFrame;
import jfreerails.client.common.ModelRootImpl;
import jfreerails.client.common.ModelRoot;
import jfreerails.client.common.ScreenHandler;
import jfreerails.client.renderer.ViewLists;
import jfreerails.client.top.GUIComponentFactoryImpl;
import jfreerails.client.top.GameLoop;
import jfreerails.client.top.ViewListsImpl;
import jfreerails.client.view.ActionRoot;
import jfreerails.network.FreerailsClient;
import jfreerails.network.FreerailsGameServer;
import jfreerails.network.SavedGamesManager;
import jfreerails.server.SavedGamesManagerImpl;
import jfreerails.server.ServerGameModelImpl;
import jfreerails.util.FreerailsProgressMonitor;
import jfreerails.util.GameModel;
import jfreerails.world.player.Player;
import jfreerails.world.top.World;


/**
 * A swing freerails client.
 * @author Luke
 *
 */
public class GUIClient extends FreerailsClient
    implements FreerailsProgressMonitor {
    private ScreenHandler screenHandler;
    private ModelRootImpl modelRoot;
    private ActionRoot actionRoot;
    private GUIComponentFactoryImpl factory;
    private ViewLists vl;
    private final String name;
    private final FreerailsProgressMonitor monitor;

    public GUIClient(String name, FreerailsProgressMonitor fm, int screenMode, DisplayMode dm)
        throws IOException {
        this.name = name;
        this.monitor = null == fm ? this : fm;
        //Set up model root and action root.
        modelRoot = new ModelRootImpl();
        modelRoot.setMoveFork(this.getMoveFork());
        modelRoot.setMoveReceiver(this);
        modelRoot.setServerCommandReceiver(this);
        actionRoot = new ActionRoot();

        //Create GUI components
        factory = new GUIComponentFactoryImpl(modelRoot, actionRoot);

        JFrame createClientJFrame = factory.createClientJFrame(name);
       
        	screenHandler= new ScreenHandler(createClientJFrame, screenMode, dm);
       
    }

    void start() {
        //          Set up world.
        SavedGamesManager gamesManager = new SavedGamesManagerImpl();
        FreerailsGameServer server = new FreerailsGameServer(gamesManager);
        String mapName = gamesManager.getNewMapNames()[0];

        ServerGameModelImpl serverGameModel = new ServerGameModelImpl();
        server.setServerGameModel(serverGameModel);

        this.connect(server, name, "password");

        server.newGame(mapName);

        while (null == this.getWorld()) {
            this.update();
            server.update();
        }

        GameModel[] models = new GameModel[] {this, server};

        //Start the gameloop
        GameLoop gameLoop = new GameLoop(screenHandler, models);
        screenHandler.apply();

        Thread t = new Thread(gameLoop);
        t.start();
    }

    public static void main(String[] args) {
        try {
            GUIClient client = new GUIClient("Test", null,
                    ScreenHandler.WINDOWED_MODE, null);
            client.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setMessage(String s) {
        System.out.println(s);
    }

    public void setValue(int i) {
        // TODO Auto-generated method stub
    }

    public void setMax(int max) {
        // TODO Auto-generated method stub
    }

    protected void newWorld(World w) {
        if (null == vl || !vl.validate(w)) {
            try {
                vl = new ViewListsImpl(w, monitor);
                monitor.finished();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        //Should be a smarter way of doing this..
        for (int player = 0; player < w.getNumberOfPlayers(); player++) {
            Player p = w.getPlayer(player);

            if (p.getName().equals(this.name)) {
                modelRoot.setup(w, p.getPrincipal());
            }
        }

        modelRoot.setProperty(ModelRoot.SERVER,
            connection2Server.getServerDetails());
        actionRoot.setup(modelRoot, vl);
        factory.setup(vl, w);
    }

    public ScreenHandler getScreenHandler() {
        return screenHandler;
    }

    public void finished() {
        // TODO Auto-generated method stub
    }
}