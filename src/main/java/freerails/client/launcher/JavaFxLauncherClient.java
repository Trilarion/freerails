/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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

package freerails.client.launcher;

import freerails.client.ActionRoot;
import freerails.client.FreerailsClient;
import freerails.client.ModelRootImpl;
import freerails.client.ModelRootProperty;
import freerails.client.UserMessageGenerator;
import freerails.client.renderer.RendererRoot;
import freerails.client.renderer.RendererRootImpl;
import freerails.client.renderer.map.detail.DetailMapRenderer;
import freerails.client.renderer.map.overview.OverviewMapRenderer;
import freerails.controller.BuildTrackController;
import freerails.model.player.Player;
import freerails.model.simulation.Speed;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.world.World;
import freerails.network.command.ClientProperty;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import java.awt.Dimension;
import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Client-side game session used by the JavaFX launcher and gameplay window.
 */
public final class JavaFxLauncherClient extends FreerailsClient {

    private static final TaggedLogger LOGGER = Logger.tag("JavaFxLauncherClient");
    private final String playerName;
    private final Object updateLock = new Object();
    private final AtomicBoolean running = new AtomicBoolean();
    private final AtomicBoolean closed = new AtomicBoolean();
    private final ModelRootImpl modelRoot = new ModelRootImpl();
    private final ActionRoot actionRoot = new ActionRoot();
    private volatile JavaFxGameplayWindow gameplayWindow;
    private volatile RendererRoot rendererRoot;
    private volatile BuildTrackController buildTrackController;
    private volatile long lastStatusUpdate;

    /**
     * Creates a JavaFX gameplay client for the selected player.
     *
     * @param playerName player name used when identifying the local player
     */
    public JavaFxLauncherClient(String playerName) {
        this.playerName = Objects.requireNonNull(playerName, "playerName");
        modelRoot.setProperty(ModelRootProperty.PLAY_SOUNDS, Boolean.FALSE);
        modelRoot.setMoveFork(getMoveFork());
        modelRoot.setMoveReceiver(this);
        modelRoot.setServerCommandReceiver(this);
        modelRoot.addCompleteMoveReceiver(new UserMessageGenerator(modelRoot));
    }

    /**
     * Attaches the JavaFX gameplay window before processing incoming game state.
     *
     * @param window JavaFX gameplay window to display the game
     */
    public void attachGameplayWindow(JavaFxGameplayWindow window) {
        gameplayWindow = Objects.requireNonNull(window, "window");
    }

    /**
     * Starts the background network and game update loop once.
     */
    public void start() {
        if (closed.get()) {
            throw new IllegalStateException("The JavaFX client has been closed.");
        }
        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("The JavaFX client is already running.");
        }
        Thread updateThread = new Thread(this::runUpdates, "JavaFX game updates");
        updateThread.setDaemon(true);
        updateThread.start();
    }

    /**
     * Stops the network and game update loop.
     */
    public void stop() {
        if (closed.compareAndSet(false, true)) {
            synchronized (updateLock) {
                running.set(false);
                disconnect();
            }
        }
    }

    /**
     * Updates sub-tick time and delegates gradual build-path searches.
     */
    @Override
    protected void clientUpdates() {
        BuildTrackController controller = buildTrackController;
        if (controller != null) {
            controller.update();
        }

        UnmodifiableWorld world = modelRoot.getWorld();
        if (world == null) {
            return;
        }

        long currentTime = System.currentTimeMillis();
        double ticks = world.getClock().getCurrentTime().ticks();
        Speed speed = world.getSpeed();
        if (speed.getTicksPerSecond() > 0) {
            ticks += Math.min(currentTime - getLastTickTime(), 1.0d);
        }
        modelRoot.setProperty(ModelRootProperty.TIME, ticks);

        if (currentTime - lastStatusUpdate >= 250) {
            lastStatusUpdate = currentTime;
            JavaFxGameplayWindow window = gameplayWindow;
            if (window != null) {
                window.updateStatus(
                        world.getClock().getCurrentDateAsString(),
                        freerails.client.MoneyFormatter.format(
                                world.getCurrentBalance(modelRoot.getPlayer())));
                window.refreshTrainList();
            }
        }
    }

    /**
     * Reports server communication failures in JavaFX without terminating the process.
     *
     * @param exception update failure
     */
    @Override
    protected void onUpdateFailure(IOException exception) {
        LOGGER.error(exception, "Could not update JavaFX client from server.");
        JavaFxGameplayWindow window = gameplayWindow;
        if (window != null) {
            window.showError("The connection to the server was lost: " + exception.getMessage());
        }
        running.set(false);
    }

    /**
     * Returns the action root used by JavaFX map controls.
     *
     * @return available game actions
     */
    ActionRoot getActionRoot() {
        return actionRoot;
    }

    /**
     * Handles a newly received game world and initializes its JavaFX views.
     *
     * @param world new game world
     */
    @Override
    protected void newWorld(World world) {
        try {
            RendererRoot currentRendererRoot = rendererRoot;
            if (currentRendererRoot == null || !currentRendererRoot.validate(world)) {
                currentRendererRoot = new RendererRootImpl(world);
                rendererRoot = currentRendererRoot;
            }

            Player localPlayer = world.getPlayers().stream()
                    .filter(player -> player.getName().equals(playerName))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "The connected game does not contain player " + playerName + "."));
            modelRoot.setup(world, localPlayer);
            modelRoot.setProperty(ModelRootProperty.SERVER, "");
            actionRoot.setup(modelRoot, currentRendererRoot);

            BuildTrackController controller = new BuildTrackController(world, modelRoot);
            buildTrackController = controller;
            DetailMapRenderer mainMap = new DetailMapRenderer(world, currentRendererRoot, modelRoot);
            var overviewMap = OverviewMapRenderer.getInstance(world, new Dimension(200, 200));
            var cursor = new freerails.client.view.FreerailsCursor(modelRoot, currentRendererRoot);

            JavaFxGameplayWindow window = Objects.requireNonNull(
                    gameplayWindow, "JavaFX gameplay window must be attached before starting the client.");
            window.setup(
                    modelRoot,
                    mainMap,
                    overviewMap,
                    cursor,
                    actionRoot,
                    controller,
                    updateLock,
                    localPlayer.getName() + " - " + modelRoot.getProperty(ModelRootProperty.SERVER) + " - Freerails");
        } catch (Exception e) {
            LOGGER.error(e, "Could not initialize JavaFX gameplay for the new world.");
            JavaFxGameplayWindow window = gameplayWindow;
            if (window != null) {
                window.showError("Could not initialize the game: " + e.getMessage());
            }
            running.set(false);
        }
    }

    /**
     * Runs regular game updates away from the JavaFX application thread.
     */
    private void runUpdates() {
        try {
            while (running.get()) {
                long frameStart = System.nanoTime();
                synchronized (updateLock) {
                    if (running.get()) {
                        update();
                    }
                }
                long remainingNanos = 25_000_000L - (System.nanoTime() - frameStart);
                if (remainingNanos > 0) {
                    Thread.sleep(remainingNanos / 1_000_000L,
                            (int) (remainingNanos % 1_000_000L));
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (running.get()) {
                LOGGER.warn(e, "JavaFX game update thread was interrupted unexpectedly.");
            }
        } catch (RuntimeException e) {
            LOGGER.error(e, "Unexpected exception in the JavaFX game update loop.");
            JavaFxGameplayWindow window = gameplayWindow;
            if (window != null) {
                window.showError("The game update loop stopped: " + e.getMessage());
            }
            running.set(false);
        }
    }

    /**
     * Processes a client property received from the local server.
     *
     * @param property server property
     * @param value serialized property value
     */
    @Override
    public void setProperty(ClientProperty property, Serializable value) {
        super.setProperty(property, value);
        if (property == ClientProperty.SAVED_GAMES) {
            modelRoot.setProperty(ModelRootProperty.SAVED_GAMES_LIST, value);
        }
    }
}
