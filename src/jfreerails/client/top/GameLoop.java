package jfreerails.client.top;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Toolkit;
import jfreerails.client.common.RepaintManagerForActiveRendering;
import jfreerails.client.common.ScreenHandler;
import jfreerails.client.common.SynchronizedEventQueue;
import jfreerails.util.GameModel;


/**
 * This thread updates the GUI Client window.
 *
 */
final public class GameLoop implements Runnable {
    final static boolean LIMIT_FRAME_RATE = false;
    boolean gameNotDone = true;
    final ScreenHandler screenHandler;
    final static int TARGET_FPS = 40;
    FPScounter fPScounter;
    private long frameStartTime;
    private final GameModel model;

    public GameLoop(ScreenHandler s) {
        screenHandler = s;
        model = GameModel.NULL_MODEL;
    }

    public GameLoop(ScreenHandler s, GameModel gm) {
        screenHandler = s;
        model = gm;

        if (null == model) {
            throw new NullPointerException();
        }
    }

    public void multiLockedCallback() {
    }

    public void run() {
        //SynchronizedEventQueue seq = SynchronizedEventQueue.getInstance();
        RepaintManagerForActiveRendering.addJFrame(screenHandler.frame);
        RepaintManagerForActiveRendering.setAsCurrentManager();

        fPScounter = new FPScounter();

        long nextModelUpdateDue = System.currentTimeMillis();

        /*
         * Reduce this threads priority to avoid starvation of the input thread
         * on Windows.
         */
        try {
            Thread.currentThread().setPriority(Thread.NORM_PRIORITY - 1);
        } catch (SecurityException e) {
            System.err.println("Couldn't lower priority of redraw thread");
        }

        while (gameNotDone) {
            frameStartTime = System.currentTimeMillis();

            if (!screenHandler.isMinimised()) {
                /*
                 * Flush all redraws in the underlying toolkit.  This reduces
                 * X11 lag when there isn't much happening, but is expensive
                 * under Windows
                 */
                Toolkit.getDefaultToolkit().sync();

                synchronized (SynchronizedEventQueue.MUTEX) {
                    model.update();

                    Graphics g = screenHandler.getDrawGraphics();

                    try {
                        screenHandler.frame.paintComponents(g);

                        fPScounter.updateFPSCounter(frameStartTime, g);
                    } finally {
                        g.dispose();
                    }

                    screenHandler.swapScreens();
                }

                if (LIMIT_FRAME_RATE) {
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
            } else {
                try {
                    //The window is minimised
                    Thread.sleep(200);
                } catch (Exception e) {
                }
            }
        }
    }
}

final class FPScounter {
    final long TIME_INTERVAL = 5000;
    int frameCount = 0;
    int averageFPS = 0;
    long averageFPSStartTime = System.currentTimeMillis();
    String fPSstr = "starting..";
    boolean dot = true;

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