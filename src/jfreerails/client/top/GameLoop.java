package jfreerails.client.top;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.util.logging.Logger;

import jfreerails.client.common.RepaintManagerForActiveRendering;
import jfreerails.controller.ReportBugTextGenerator;
import jfreerails.controller.ScreenHandler;
import jfreerails.util.GameModel;

/**
 * This thread updates the GUI Client window.
 * 
 * @author Luke
 */
final public class GameLoop implements Runnable {

	private static final Logger logger = Logger.getLogger(GameLoop.class.getName());

	private final static boolean LIMIT_FRAME_RATE = false;

	private boolean gameNotDone = false;

	private final ScreenHandler screenHandler;

	private final static int TARGET_FPS = 40;

	private FPScounter fPScounter;

	private long frameStartTime;

	private final GameModel[] model;

	private final Integer loopMonitor = new Integer(0);

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
			RepaintManagerForActiveRendering.addJFrame(screenHandler.frame);
			RepaintManagerForActiveRendering.setAsCurrentManager();

			if (!screenHandler.isInUse()) {
				screenHandler.apply();
			}
			
			gameNotDone = true;

			fPScounter = new FPScounter();

			/*
			 * Reduce this threads priority to avoid starvation of the input
			 * thread on Windows.
			 */
			try {
				Thread.currentThread().setPriority(Thread.NORM_PRIORITY - 1);
			} catch (SecurityException e) {
				logger.warning("Couldn't lower priority of redraw thread");
			}

			while (true) {
				// stats.record();
				frameStartTime = System.currentTimeMillis();

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

					for (int i = 0; i < model.length; i++) {
						model[i].update();
					}

					if (!screenHandler.isMinimised()) {
						if (screenHandler.isInUse()) {
							Graphics g = screenHandler.getDrawGraphics();

							try {
							    
								screenHandler.frame.paintComponents(g);

								boolean showFps = Boolean.parseBoolean(System
										.getProperty("SHOWFPS"));
								if (showFps) {
									fPScounter.drawFPS((Graphics2D) g);
								}
							} catch (RuntimeException re) {
								/*
								 * We are not expecting a RuntimeException here.
								 * If something goes wrong, lets kill the game
								 * straight away to avoid hard-to-track-down
								 * bugs.
								 */
								ReportBugTextGenerator.unexpectedException(re);
							} finally {
								g.dispose();
							}

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
					long deltatime = System.currentTimeMillis() - frameStartTime;

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
			}

			/* signal that we are done */
			synchronized (loopMonitor) {
				loopMonitor.notify();
			}
		} catch (Exception e) {
			ReportBugTextGenerator.unexpectedException(e);
		}
	}
}

/**
 * Provides a method that draws a String showing the average FPS over the last
 * complete 5000ms interval.
 * 
 * @author Luke
 * 
 */
final class FPScounter {

	private final double[] fpsValues = new double[400];

	private int newFrameCount = 0;

	private String newFPSstr = "starting..";

	private long lastFrameTime;

	private final int fontSize;

	private final Color bgColor;

	FPScounter() {
		this.fontSize = 10;
		bgColor = new Color(0, 0, 128);
	}

	// Display the average number of FPS.
	void updateFPSCounter() {
		long currentTime = System.nanoTime();

		if (newFrameCount == 0) {
			lastFrameTime = currentTime;
		}
		double dt = currentTime - lastFrameTime;
		double fps = 1000000000d / dt;
		fpsValues[newFrameCount % fpsValues.length] = fps;
		newFrameCount++;

		int n = fpsValues.length;
		if (newFrameCount > fpsValues.length) {
			double min = Double.MAX_VALUE;
			double max = Double.MIN_VALUE;

			double mean = 0;
			for (int i = 0; i < fpsValues.length; i++) {
				min = Math.min(min, fpsValues[i]);
				max = Math.max(max, fpsValues[i]);
				mean += fpsValues[i];
			}
			mean = mean / n;
			if (mean > max)
				throw new IllegalStateException();

			if (mean < min)
				throw new IllegalStateException();

			double variance = 0;
			for (int i = 0; i < fpsValues.length; i++) {
				double xMinusU = fpsValues[i] - mean;
				variance += xMinusU * xMinusU;
			}
			variance = variance / n;
			if (newFrameCount % 20 == 0) {
				StringBuffer sb = new StringBuffer();
				sb.append("FPS\n");
				sb.append(" n  ");
				sb.append(n);
				sb.append('\n');
				sb.append(" \u03BC  ");
				sb.append(Math.round(mean));
				sb.append('\n');
				sb.append(" \u03C3  ");
				sb.append(Math.round(Math.sqrt(variance)));
				sb.append('\n');
				sb.append(" min  ");
				sb.append(Math.round(min));
				sb.append('\n');
				sb.append(" max  ");
				sb.append(Math.round(max));
				sb.append('\n');
				sb.append(" Last ");
				sb.append(Math.round(fps));
                sb.append('\n');

				newFPSstr = sb.toString();

			}

		}

		// g.setColor(Color.WHITE);
		// g.fillRect(50, 50, 50, 20);
		// g.setColor(Color.BLACK);
		// g.drawString(newFPSstr, 50, 65);
		lastFrameTime = currentTime;
	}

	void drawFPS(Graphics2D g) {
		int rectWidth;
		int rectHeight;
		int rectX;
		int rectY;

		int positionX = 50;
		int positionY = 70;

		Color textColor = Color.WHITE;

		String[] lines = newFPSstr.split("\n");
		rectWidth = 60;
		rectHeight = (int) ((fontSize + 1) * 1.2 * lines.length);
		rectY = (int) (positionY - fontSize * 1.2);
		rectX = positionX;

		g.setColor(bgColor);
		g.fillRect(rectX, rectY, rectWidth, rectHeight);

		g.setColor(textColor);
		// g.setFont(font);
		for (String s : lines) {
			g.drawString(s, positionX, positionY);
			positionY += fontSize * 1.2;
		}
	}

}