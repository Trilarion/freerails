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

import freerails.client.*;
import freerails.client.componentfactory.GUIComponentFactoryImpl;
import freerails.client.renderer.RendererRoot;
import freerails.client.renderer.RendererRootImpl;
import freerails.client.view.ActionRoot;
import freerails.controller.ClientProperty;
import freerails.controller.ModelRoot;
import freerails.controller.ModelRoot.Property;
import freerails.util.ui.ProgressMonitorModel;
import freerails.model.ITEM;
import freerails.model.world.ReadOnlyWorld;
import freerails.model.world.World;
import freerails.model.game.GameSpeed;
import freerails.model.game.GameTime;
import freerails.model.player.Player;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.Serializable;

/**
 * A swing freerails client.
 */
public class LauncherClient extends FreerailsClient {

    private final ActionRoot actionRoot;
    private final GUIComponentFactoryImpl factory;
    private final ModelRootImpl modelRoot;
    private final ProgressMonitorModel monitor;
    private final String name;
    private final ScreenHandler screenHandler;
    private RendererRoot rendererRoot;

    /**
     * @param name
     * @param progressMonitorModel
     * @param screenMode
     * @param displayMode
     * @throws IOException
     */
    public LauncherClient(String name, ProgressMonitorModel progressMonitorModel, int screenMode, DisplayMode displayMode) {
        this.name = name;
        monitor = null == progressMonitorModel ? ProgressPanelModel.EMPTY : progressMonitorModel;
        // Set up model root and action root.
        modelRoot = new ModelRootImpl();
        modelRoot.setMoveFork(getMoveFork());
        modelRoot.setMoveReceiver(this);
        modelRoot.setServerCommandReceiver(this);
        actionRoot = new ActionRoot(modelRoot);

        // Create GUI components
        factory = new GUIComponentFactoryImpl(modelRoot, actionRoot);
        JFrame createClientJFrame = factory.createClientJFrame(name);
        screenHandler = new ScreenHandler(createClientJFrame, screenMode, displayMode);
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
                subTicks = Math.min(dt, 1.0d);
                ticks += subTicks;
            }
            modelRoot.setProperty(Property.TIME, ticks);
        }
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
            if (null == rendererRoot || !rendererRoot.validate(w)) {
                    rendererRoot = new RendererRootImpl(w, monitor);
                    monitor.finished();
            }

            // Should be a smarter way of doing this..
            for (int player = 0; player < w.getNumberOfPlayers(); player++) {
                Player p = w.getPlayer(player);

                if (p.getName().equals(name)) {
                    modelRoot.setup(w, p.getPrincipal());
                }
            }

            modelRoot.setProperty(ModelRoot.Property.SERVER, connectionToServer.getServerDetails());
            actionRoot.setup(modelRoot, rendererRoot);

            factory.setup(rendererRoot, w);
        } catch (Exception e) {
            LauncherFrame.emergencyStop();
        }
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
}