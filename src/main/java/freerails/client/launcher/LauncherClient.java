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
import freerails.client.model.ServerControlModel;
import freerails.client.renderer.RendererRoot;
import freerails.client.renderer.RendererRootImpl;
import freerails.controller.BuildTrackController;
import freerails.model.player.Player;
import freerails.network.command.ClientProperty;
import freerails.util.Vec2D;
import freerails.model.world.WorldItem;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.world.World;
import freerails.model.game.GameSpeed;
import freerails.model.game.GameTime;

import javax.swing.*;
import java.io.IOException;
import java.io.Serializable;

/**
 * A swing freerails client.
 */
public class LauncherClient extends FreerailsClient {

    private final ActionRoot actionRoot;
    private final GUIComponentFactoryImpl factory;
    private final ModelRootImpl modelRoot;
    private final String name;
    private final ScreenHandler screenHandler;
    private RendererRoot rendererRoot;

    /**
     * @param name
     * @param screenMode
     * @param displayMode
     * @throws IOException
     */
    public LauncherClient(String name, int screenMode, Vec2D displayMode) {
        this.name = name;
        // Set up model root and action root.
        modelRoot = new ModelRootImpl();
        modelRoot.setMoveFork(getMoveFork());
        modelRoot.setMoveReceiver(this);
        modelRoot.setServerCommandReceiver(this);
        actionRoot = new ActionRoot(new ServerControlModel(modelRoot));

        // Create GUI components
        factory = new GUIComponentFactoryImpl(modelRoot, actionRoot);
        JFrame createClientJFrame = factory.createClientJFrame(name);
        screenHandler = new ScreenHandler(createClientJFrame, screenMode, displayMode);
    }

    @Override
    protected void clientUpdates() {
        BuildTrackController buildTrackController = factory.getBuildTrackController();
        if (buildTrackController != null) {
            buildTrackController.update();
        }
        // Update sub tick time.
        long currentTime = System.currentTimeMillis();
        long lastTick = getLastTickTime();
        double dt = currentTime - lastTick;
        UnmodifiableWorld world2 = modelRoot.getWorld();
        if (world2 != null) {
            GameSpeed gameSpeed = (GameSpeed) world2.get(WorldItem.GameSpeed);
            GameTime currentGameTime = world2.currentTime();
            double ticks = currentGameTime.getTicks();
            if (!gameSpeed.isPaused()) {
                double subTicks = Math.min(dt, 1.0d);
                ticks += subTicks;
            }
            modelRoot.setProperty(ModelRootProperty.TIME, ticks);
        }
    }

    /**
     * @return
     */
    public ScreenHandler getScreenHandler() {
        return screenHandler;
    }

    @Override
    protected void newWorld(World world) {
        try {
            if (null == rendererRoot || !rendererRoot.validate(world)) {
                    rendererRoot = new RendererRootImpl(world);
            }

            // Should be a smarter way of doing this..
            for (int i = 0; i < world.getNumberOfPlayers(); i++) {
                Player player = world.getPlayer(i);

                if (player.getName().equals(name)) {
                    modelRoot.setup(world, player);
                }
            }

            modelRoot.setProperty(ModelRootProperty.SERVER, ""); // TODO display ip plus port here using connectionToServer
            actionRoot.setup(modelRoot, rendererRoot);

            factory.setup(rendererRoot, world);
        } catch (Exception e) {
            e.printStackTrace();
            LauncherFrame.emergencyStop();
        }
    }

    @Override
    public void setProperty(ClientProperty property, Serializable value) {
        super.setProperty(property, value);
        switch (property) {
            case SAVED_GAMES:
                modelRoot.setProperty(ModelRootProperty.SAVED_GAMES_LIST, value);
                break;

            default:
                break;
        }
    }
}