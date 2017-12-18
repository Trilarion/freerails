/*
 * Created on Sep 11, 2004
 *
 */
package freerails.launcher;

import freerails.client.common.ModelRootImpl;
import freerails.client.renderer.RenderersRoot;
import freerails.client.top.GUIComponentFactoryImpl;
import freerails.client.top.GameLoop;
import freerails.client.top.RenderersRootImpl;
import freerails.client.view.ActionRoot;
import freerails.controller.ModelRoot;
import freerails.controller.ModelRoot.Property;
import freerails.controller.ReportBugTextGenerator;
import freerails.controller.ScreenHandler;
import freerails.network.FreerailsClient;
import freerails.network.FreerailsGameServer;
import freerails.network.SavedGamesManager;
import freerails.server.SavedGamesManagerImpl;
import freerails.server.ServerGameModelImpl;
import freerails.util.FreerailsProgressMonitor;
import freerails.util.GameModel;
import freerails.world.common.GameSpeed;
import freerails.world.common.GameTime;
import freerails.world.player.Player;
import freerails.world.top.ITEM;
import freerails.world.top.ReadOnlyWorld;
import freerails.world.top.World;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.Serializable;

/**
 * A swing freerails client.
 *
 * @author Luke
 */
public class GUIClient extends FreerailsClient implements
        FreerailsProgressMonitor {

    private final ActionRoot actionRoot;
    private final GUIComponentFactoryImpl factory;
    private final ModelRootImpl modelRoot;
    private final FreerailsProgressMonitor monitor;
    private final String name;
    private final ScreenHandler screenHandler;
    private RenderersRoot vl;

    /**
     *
     * @param name
     * @param fm
     * @param screenMode
     * @param dm
     * @throws IOException
     */
    public GUIClient(String name, FreerailsProgressMonitor fm, int screenMode,
                     DisplayMode dm) throws IOException {
        this.name = name;
        this.monitor = null == fm ? this : fm;
        // Set up model root and action root.
        modelRoot = new ModelRootImpl();
        modelRoot.setMoveFork(this.getMoveFork());
        modelRoot.setMoveReceiver(this);
        modelRoot.setServerCommandReceiver(this);
        actionRoot = new ActionRoot(modelRoot);

        // Create GUI components
        factory = new GUIComponentFactoryImpl(modelRoot, actionRoot);

        JFrame createClientJFrame = factory.createClientJFrame(name);

        screenHandler = new ScreenHandler(createClientJFrame, screenMode, dm);

    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            GUIClient client = new GUIClient("Test", null,
                    ScreenHandler.WINDOWED_MODE, null);
            client.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void clientUpdates() {
        if (factory.isSetup()) {
            factory.getBuildTrackController().update();
            // Update sub tick time.
            long currentTime = System.currentTimeMillis();
            long lastTick = getLastTickTime();
            double dt = currentTime - lastTick;
            ReadOnlyWorld world2 = modelRoot.getWorld();
            GameSpeed gameSpeed = (GameSpeed) world2.get(ITEM.GAME_SPEED);
            GameTime currentGameTime = world2.currentTime();
            double ticks = currentGameTime.getTicks();
            if (!gameSpeed.isPaused()) {
                double milliSecondsPerTick = 1000 / gameSpeed.getSpeed();
                double subTicks = dt / milliSecondsPerTick;
                subTicks = Math.min(dt, 1d);
                ticks += subTicks;
            }
            modelRoot.setProperty(Property.TIME, ticks);
        }

    }

    /**
     *
     */
    public void finished() {
        // TODO Auto-generated method stub
    }

    /**
     *
     * @return
     */
    public ScreenHandler getScreenHandler() {
        return screenHandler;
    }

    @Override
    protected void newWorld(World w) {
        try {
            if (null == vl || !vl.validate(w)) {
                try {
                    vl = new RenderersRootImpl(w, monitor);
                    monitor.finished();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            // Should be a smarter way of doing this..
            for (int player = 0; player < w.getNumberOfPlayers(); player++) {
                Player p = w.getPlayer(player);

                if (p.getName().equals(this.name)) {
                    modelRoot.setup(w, p.getPrincipal());
                }
            }

            modelRoot.setProperty(ModelRoot.Property.SERVER, connection2Server
                    .getServerDetails());
            actionRoot.setup(modelRoot, vl);

            factory.setup(vl, w);
        } catch (Exception e) {
            ReportBugTextGenerator.unexpectedException(e);
        }
    }

    /**
     *
     * @param max
     */
    public void nextStep(int max) {
        // TODO Auto-generated method stub
    }

    /**
     *
     * @param s
     */
    public void setMessage(String s) {
        System.out.println(s);
    }

    /**
     *
     * @param i
     */
    public void setValue(int i) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setProperty(ClientProperty propertyName, Serializable value) {
        super.setProperty(propertyName, value);
        switch (propertyName) {
            case SAVED_GAMES:
                modelRoot.setProperty(Property.SAVED_GAMES_LIST, value);
                break;

            default:
                break;
        }
    }

    void start() {
        // Set up world.
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

        GameModel[] models = new GameModel[]{this, server};

        // Start the game loop
        GameLoop gameLoop = new GameLoop(screenHandler, models);

        Thread t = new Thread(gameLoop);
        t.start();
    }
}