/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 *
 */
package freerails.client.launcher;

import freerails.client.common.ModelRootImpl;
import freerails.client.renderer.RendererRoot;
import freerails.client.top.GUIComponentFactoryImpl;
import freerails.client.top.GameLoop;
import freerails.client.top.RendererRootImpl;
import freerails.client.view.ActionRoot;
import freerails.controller.ModelRoot;
import freerails.controller.ModelRoot.Property;
import freerails.controller.ReportBugTextGenerator;
import freerails.controller.ScreenHandler;
import freerails.network.FreerailsClient;
import freerails.network.FreerailsGameServer;
import freerails.network.SaveGamesManager;
import freerails.server.SaveGameManagerImpl;
import freerails.server.ServerGameModelImpl;
import freerails.client.ProgressMonitorModel;
import freerails.world.*;
import freerails.world.game.GameModel;
import freerails.world.game.GameSpeed;
import freerails.world.game.GameTime;
import freerails.world.player.Player;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.Serializable;

/**
 * A swing freerails client.
 */
public class GUIClient extends FreerailsClient implements
        ProgressMonitorModel {

    private final ActionRoot actionRoot;
    private final GUIComponentFactoryImpl factory;
    private final ModelRootImpl modelRoot;
    private final ProgressMonitorModel monitor;
    private final String name;
    private final ScreenHandler screenHandler;
    private RendererRoot vl;

    /**
     * @param name
     * @param fm
     * @param screenMode
     * @param dm
     * @throws IOException
     */
    public GUIClient(String name, ProgressMonitorModel fm, int screenMode,
                     DisplayMode dm) {
        this.name = name;
        monitor = null == fm ? this : fm;
        // Set up model root and action root.
        modelRoot = new ModelRootImpl();
        modelRoot.setMoveFork(getMoveFork());
        modelRoot.setMoveReceiver(this);
        modelRoot.setServerCommandReceiver(this);
        actionRoot = new ActionRoot(modelRoot);

        // Create GUI components
        factory = new GUIComponentFactoryImpl(modelRoot, actionRoot);

        JFrame createClientJFrame = factory.createClientJFrame(name);

        screenHandler = new ScreenHandler(createClientJFrame, screenMode, dm);

    }

    /**
     * @param args
     */
    public static void main(String[] args) {
            GUIClient client = new GUIClient("Test", null,
                    ScreenHandler.WINDOWED_MODE, null);
            client.start();
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
                double subTicks;
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
                    vl = new RendererRootImpl(w, monitor);
                    monitor.finished();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                }
            }

            // Should be a smarter way of doing this..
            for (int player = 0; player < w.getNumberOfPlayers(); player++) {
                Player p = w.getPlayer(player);

                if (p.getName().equals(name)) {
                    modelRoot.setup(w, p.getPrincipal());
                }
            }

            modelRoot.setProperty(ModelRoot.Property.SERVER, connectionToServer
                    .getServerDetails());
            actionRoot.setup(modelRoot, vl);

            factory.setup(vl, w);
        } catch (Exception e) {
            ReportBugTextGenerator.unexpectedException(e);
        }
    }

    /**
     * @param max
     */
    public void nextStep(int max) {
        // TODO Auto-generated method stub
    }

    /**
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
        SaveGamesManager gamesManager = new SaveGameManagerImpl();
        FreerailsGameServer server = new FreerailsGameServer(gamesManager);
        String mapName = gamesManager.getNewMapNames()[0];

        ServerGameModelImpl serverGameModel = new ServerGameModelImpl();
        server.setServerGameModel(serverGameModel);

        connect(server, name, "password");

        server.newGame(mapName);

        while (null == getWorld()) {
            update();
            server.update();
        }

        GameModel[] models = new GameModel[]{this, server};

        // Start the game loop
        GameLoop gameLoop = new GameLoop(screenHandler, models);

        Thread t = new Thread(gameLoop);
        t.start();
    }
}