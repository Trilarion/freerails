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

import freerails.client.ClientConstants;
import freerails.client.SynchronizedEventQueue;
import freerails.util.ui.RepaintManagerForActiveRendering;
import freerails.util.Utils;
import freerails.model.GameModel;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import java.awt.*;

/**
 * This thread updates the GUI Client window.
 */
public class GameLoop implements Runnable {

    private static final TaggedLogger logger = Logger.tag("GameLoop");

    private final ScreenHandler screenHandler;
    private final GameModel gameModel;

    /**
     * Creates a game loop instance.
     *
     * @param screenHandler the screen handler parameter
     */
    public GameLoop(ScreenHandler screenHandler) {
        this.screenHandler = screenHandler;
        gameModel = null;
    }

    /**
     * Creates a game loop instance.
     *
     * @param screenHandler the screen handler parameter
     * @param gameModel the game model parameter
     */
    public GameLoop(ScreenHandler screenHandler, GameModel gameModel) {
        this.screenHandler = screenHandler;
        this.gameModel = Utils.verifyNotNull(gameModel);
    }

    /**
     * Runs this task.
     */
    @Override
    public void run() {
        try {

            SynchronizedEventQueue.use();
            RepaintManagerForActiveRendering.addJFrame(screenHandler.frame);
            RepaintManagerForActiveRendering.setAsCurrentManager();

            if (!screenHandler.isInUse()) {
                screenHandler.apply();
            }

            boolean gameNotDone = true;

            /*
             * Reduce this threads priority to avoid starvation of the input
             * thread on Windows.
             */
            try {
                Thread.currentThread().setPriority(Thread.NORM_PRIORITY - 1);
            } catch (SecurityException e) {
                logger.warn("Couldn't lower priority of redraw thread");
            }

            while (true) {
                long frameStartTime = System.currentTimeMillis();

                /*
                 * Flush all redraws in the underlying toolkit. This reduces X11
                 * lag when there isn't much happening, but is expensive under
                 * Windows
                 */
                Toolkit.getDefaultToolkit().sync();

                synchronized (SynchronizedEventQueue.MUTEX) {
                    if (!gameNotDone) {
                        SynchronizedEventQueue.MUTEX.notify();
                        break;
                    }

                    if (null != gameModel) {
                        gameModel.update();
                    }

                    if (!screenHandler.isMinimised()) {
                        if (screenHandler.isInUse()) {
                            boolean contentsRestored;
                            do {
                                Graphics g = screenHandler.getDrawGraphics();

                                try {

                                    screenHandler.frame.paintComponents(g);

                                } catch (RuntimeException re) {
                                    /*
                                     * We are not expecting a RuntimeException
                                     * here. If something goes wrong, lets kill
                                     * the game straight away to avoid
                                     * hard-to-track-down bugs.
                                     */
                                    logger.error(re, "Unexpected exception while painting the game window.");
                                    LauncherFrame.emergencyStop();
                                } finally {
                                    g.dispose();
                                }
                                contentsRestored = screenHandler.contentsRestored();
                            } while (contentsRestored);
                            screenHandler.swapScreens();
                        }
                    }
                }

                if (screenHandler.isMinimised()) {
                    try {
                        // The window is minimised so we don't need to keep updating.
                        Thread.sleep(200);
                    } catch (Exception e) {}
                } else if (ClientConstants.LIMIT_FRAME_RATE) {
                    long deltatime = System.currentTimeMillis() - frameStartTime;

                    while (deltatime < 1000 / ClientConstants.TARGET_FPS) {
                        try {
                            long sleeptime = 1000 / ClientConstants.TARGET_FPS - deltatime;
                            Thread.sleep(sleeptime);
                        } catch (Exception ignored) {
                        }

                        deltatime = System.currentTimeMillis() - frameStartTime;
                    }
                }
                // remove all events from an event queue (max 5ms)
                long startEventWaitTime = System.currentTimeMillis() + 4;
                while (SynchronizedEventQueue.getInstance().peekEvent() != null) {
                    // we have events
                    Thread.yield();
                    if (startEventWaitTime < System.currentTimeMillis()) {
                        break;
                    }
                }
                //      Thread.sleep(5);
            }

            // signal that we are done
            final Object loopMonitor = new Object();
            synchronized (loopMonitor) {
                loopMonitor.notify();
            }
        } catch (Exception e) {
            logger.error(e, "Unexpected exception in the game loop.");
            LauncherFrame.emergencyStop();
        } finally {
            RepaintManagerForActiveRendering.removeJFrame(screenHandler.frame);
        }
    }
}