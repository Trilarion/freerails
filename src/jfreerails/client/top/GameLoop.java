package jfreerails.client.top;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.util.logging.Logger;
import jfreerails.client.common.RepaintManagerForActiveRendering;
import jfreerails.client.common.ScreenHandler;
import jfreerails.client.common.SynchronizedEventQueue;
import jfreerails.util.GameModel;


/**
 * This thread updates the GUI Client window.
 *  @author Luke
 */
final public class GameLoop implements Runnable {
    /** Whether to display the FPS counter on the top left of the screen.*/
    private static final Logger logger = Logger.getLogger(GameLoop.class.getName());
    private boolean SHOWFPS = (System.getProperty("SHOWFPS") != null);
    private final static boolean LIMIT_FRAME_RATE = false;
    private boolean gameNotDone = false;
    private final ScreenHandler screenHandler;
    private final static int TARGET_FPS = 40;
    private FPScounter fPScounter;
    private long frameStartTime;
    private final GameModel[] model;
    private final Integer loopMonitor = new Integer(0);

    //PerformanceStats stats = new PerformanceStats();
    public GameLoop(ScreenHandler s) {
        screenHandler = s;
        model = new GameModel[0];
    }

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

            if (!screenHandler.isInUse()) {
                screenHandler.apply();
            }

            gameNotDone = true;
            RepaintManagerForActiveRendering.addJFrame(screenHandler.frame);
            RepaintManagerForActiveRendering.setAsCurrentManager();

            fPScounter = new FPScounter();

            /*
            * Reduce this threads priority to avoid starvation of the input thread
            * on Windows.
            */
            try {
                Thread.currentThread().setPriority(Thread.NORM_PRIORITY - 1);
            } catch (SecurityException e) {
                logger.warning("Couldn't lower priority of redraw thread");
            }

            while (true) {
                //stats.record();
                frameStartTime = System.currentTimeMillis();

                /*
                * Flush all redraws in the underlying toolkit.  This reduces
                * X11 lag when there isn't much happening, but is expensive
                * under Windows
                */
                Toolkit.getDefaultToolkit().sync();

                synchronized (SynchronizedEventQueue.MUTEX) {
                    if (!gameNotDone) {
                        SynchronizedEventQueue.MUTEX.notify();

                        break;
                    }

                    for (int i = 0; i < model.length; i++) {
                        model[i].update();
                    }

                    if (!screenHandler.isMinimised()) {
                        if (screenHandler.isInUse()) {
                            Graphics g = screenHandler.getDrawGraphics();

                            try {
                                screenHandler.frame.paintComponents(g);

                                if (SHOWFPS) {
                                    fPScounter.updateFPSCounter(frameStartTime,
                                        g);
                                }
                            } catch (RuntimeException re) {
                                /* We are not expecting a RuntimeException here.
                                * If something goes wrong, lets kill the game straight
                                * away to avoid hard-to-track-down bugs.
                                */
                                logger.severe(
                                    "Unexpected exception, quitting..");
                                re.printStackTrace();
                                System.exit(1);
                            } finally {
                                g.dispose();
                            }

                            screenHandler.swapScreens();
                        }
                    }
                }

                if (screenHandler.isMinimised()) {
                    try {
                        //The window is minimised so we don't need to keep updating.
                        Thread.sleep(200);
                    } catch (Exception e) {
                        //do nothing.
                    }
                } else if (LIMIT_FRAME_RATE) {
                    long deltatime = System.currentTimeMillis() -
                        frameStartTime;

                    while (deltatime < (1000 / TARGET_FPS)) {
                        try {
                            long sleeptime = (1000 / TARGET_FPS) - deltatime;
                            Thread.sleep(sleeptime);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        deltatime = System.currentTimeMillis() -
                            frameStartTime;
                    }
                }
            }

            /* signal that we are done */
            synchronized (loopMonitor) {
                loopMonitor.notify();
            }
        } catch (Exception e) {
            logger.severe("Unexpected exception, quitting..");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
/**
 * Provides a method that draws a String showing the average FPS over the last complete 5000ms interval.
 * @author Luke
 *
 */
final class FPScounter {
    private final long TIME_INTERVAL = 5000;
    private int frameCount = 0;
    private int averageFPS = 0;
    private long averageFPSStartTime = System.currentTimeMillis();
    private String fPSstr = "starting..";
    private boolean dot = true;

    //Display the average number of FPS.
    void updateFPSCounter(long frameStartTime, Graphics g) {
        if (frameCount == 0) {
            averageFPSStartTime = frameStartTime;
        }

        frameCount++;

        if (averageFPSStartTime + TIME_INTERVAL < frameStartTime) {
            int time = (int)(frameStartTime - averageFPSStartTime);

            if (0 != time) {
                averageFPS = frameCount * 1000 / time;
            }

            if (dot) {
                fPSstr = averageFPS + " FPS";
            } else {
                fPSstr = averageFPS + ":FPS";
            }

            frameCount = 0;
            dot = !dot;
        }

        g.setColor(Color.WHITE);
        g.fillRect(50, 50, 50, 20);
        g.setColor(Color.BLACK);
        g.drawString(fPSstr, 50, 65);
    }
}