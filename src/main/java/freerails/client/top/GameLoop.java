package freerails.client.top;

import freerails.client.common.RepaintManagerForActiveRendering;
import freerails.controller.ReportBugTextGenerator;
import freerails.controller.ScreenHandler;
import freerails.util.GameModel;
import org.apache.log4j.Logger;

import java.awt.*;

/**
 * This thread updates the GUI Client window.
 *
 * @author Luke
 */
final public class GameLoop implements Runnable {

    private static final Logger logger = Logger.getLogger(GameLoop.class
            .getName());

    private final static boolean LIMIT_FRAME_RATE = false;
    private final static int TARGET_FPS = 40;
    private final ScreenHandler screenHandler;
    private final GameModel[] model;

    /**
     *
     * @param s
     */
    public GameLoop(ScreenHandler s) {
        screenHandler = s;
        model = new GameModel[0];
    }

    /**
     *
     * @param s
     * @param gm
     */
    public GameLoop(ScreenHandler s, GameModel[] gm) {
        screenHandler = s;
        model = gm;

        if (null == model) {
            throw new NullPointerException();
        }
    }

    public void run() {
        try {

            SynchronizedEventQueue.use();
            RepaintManagerForActiveRendering.addJFrame(screenHandler.frame);
            RepaintManagerForActiveRendering.setAsCurrentManager();

            if (!screenHandler.isInUse()) {
                screenHandler.apply();
            }

            boolean gameNotDone = true;

            FPScounter fPScounter = new FPScounter();

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
                // stats.record();
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

                    for (GameModel aModel : model) {
                        aModel.update();
                    }

                    if (!screenHandler.isMinimised()) {
                        if (screenHandler.isInUse()) {
                            boolean contentsRestored = false;
                            do {
                                Graphics g = screenHandler.getDrawGraphics();

                                try {

                                    screenHandler.frame.paintComponents(g);

                                    boolean showFps = Boolean
                                            .parseBoolean(System
                                                    .getProperty("SHOWFPS"));
                                    if (showFps) {
                                        fPScounter.drawFPS((Graphics2D) g);
                                    }
                                } catch (RuntimeException re) {
                                    /*
                                     * We are not expecting a RuntimeException
                                     * here. If something goes wrong, lets kill
                                     * the game straight away to avoid
                                     * hard-to-track-down bugs.
                                     */
                                    ReportBugTextGenerator
                                            .unexpectedException(re);
                                } finally {
                                    g.dispose();
                                }
                                contentsRestored = screenHandler.contentsRestored();
                            } while (contentsRestored);
                            screenHandler.swapScreens();
                            fPScounter.updateFPSCounter();
                        }
                    }
                }

                if (screenHandler.isMinimised()) {
                    try {
                        // The window is minimised so we don't need to keep
                        // updating.
                        Thread.sleep(200);
                    } catch (Exception e) {
                        // do nothing.
                    }
                } else if (LIMIT_FRAME_RATE) {
                    long deltatime = System.currentTimeMillis()
                            - frameStartTime;

                    while (deltatime < (1000 / TARGET_FPS)) {
                        try {
                            long sleeptime = (1000 / TARGET_FPS) - deltatime;
                            Thread.sleep(sleeptime);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        deltatime = System.currentTimeMillis() - frameStartTime;
                    }
                }
                // remove all events from a event queue (max 5ms)
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

            /* signal that we are done */
            Integer loopMonitor = 0;
            synchronized (loopMonitor) {
                loopMonitor.notify();
            }
        } catch (Exception e) {
            ReportBugTextGenerator.unexpectedException(e);
        }
    }
}