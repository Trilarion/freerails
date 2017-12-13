package jfreerails.client.common;

import java.awt.Graphics;
import java.awt.Color;

/**
 * Paints a frame counter on the graphics context
 */
public final class FPSCounter {
    final long TIME_INTERVAL = 5000;
    int frameCount = 0;
    int averageFPS = 0;
    long averageFPSStartTime = System.currentTimeMillis();
    String fPSstr = "starting..";
    boolean dot = true;

    //Display the average number of FPS.
    /**
     * Paint the frame counter and update the FPS
     */
    public void updateFPSCounter(Graphics g) {
	long frameStartTime = System.currentTimeMillis();
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

