package jfreerails.client.top;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Toolkit;

import jfreerails.client.common.MultiLockedRegion;
import jfreerails.client.common.SynchronizedEventQueue;
import jfreerails.client.common.RepaintManagerForActiveRendering;
import jfreerails.client.common.ScreenHandler;

/**
 * This thread updates the GUI Client window.
 * 
 */
final public class GameLoop implements Runnable, MultiLockedRegion {

	final static boolean LIMIT_FRAME_RATE = false;

	boolean gameNotDone = true;

	final ScreenHandler screenHandler;

	final static int TARGET_FPS = 30;

	FPScounter fPScounter;

	private long frameStartTime;

	public GameLoop(ScreenHandler s) {
		screenHandler = s;
	}

	public void multiLockedCallback() {
	    Graphics g = screenHandler.getDrawGraphics();

	    try {

		screenHandler.frame.paintComponents(g);

		fPScounter.updateFPSCounter(frameStartTime, g);

	    } finally {
		g.dispose();
	    }
	    screenHandler.swapScreens();
	}

    public void run() {
	SynchronizedEventQueue seq = (SynchronizedEventQueue)
	    Toolkit.getDefaultToolkit().getSystemEventQueue();

	RepaintManagerForActiveRendering.addJFrame(screenHandler.frame);
	RepaintManagerForActiveRendering.setAsCurrentManager();

	fPScounter = new FPScounter();

	long nextModelUpdateDue = System.currentTimeMillis();

	while (gameNotDone) {

	    frameStartTime = System.currentTimeMillis();

	    if (!screenHandler.isMinimised()) {

		seq.grabAllLocks(this);

		Toolkit.getDefaultToolkit().sync();

		if (LIMIT_FRAME_RATE) {
		    long deltatime =
			System.currentTimeMillis() - frameStartTime;

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

			int time = (int) (frameStartTime - averageFPSStartTime);
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
