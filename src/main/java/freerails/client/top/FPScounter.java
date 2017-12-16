package freerails.client.top;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.LinkedList;

/**
 * Provides a method that draws a String showing the average FPS over the last
 * complete 5000ms interval.
 * 
 * @author Luke
 * 
 */
public class FPScounter {

    private static final int MAX_FPS_VALUES = 100;

    private LinkedList<Double> fpsValues;

    private int newFrameCount = 0;

    private long lastFrameTime;

    private final int fontSize;

    private final Color bgColor;

    private double mean;

    private String[] newFPSstr;

    FPScounter() {
        fpsValues = new LinkedList<Double>();
        for (int i = 0; i < MAX_FPS_VALUES; i++) {
            fpsValues.add(0.0);
        }
        this.fontSize = 10;
        bgColor = new Color(0, 0, 128);
        mean = 0.0;
        newFPSstr = new String[3];
    }

    // Display the average number of FPS.
    void updateFPSCounter() {
        long currentTime = System.nanoTime();

        if (lastFrameTime == 0) {
            lastFrameTime = currentTime;
            return;
        }
        double dt = currentTime - lastFrameTime;
        if (dt != 0.0) {
            double fps = 1000000000d / dt;
            double oldfps = fpsValues.removeFirst();
            fpsValues.addLast(fps);
            newFrameCount++;
            mean = mean - oldfps + fps;
            if (newFrameCount % 20 == 0) {
                newFPSstr[0] = "FPS\n";
                newFPSstr[1] = " \u03BC  " + Math.round(mean / MAX_FPS_VALUES);
                newFPSstr[2] = " Last " + Math.round(fps);
            }
        }
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

        rectWidth = 60;
        rectHeight = (int) ((fontSize + 1) * 1.2 * newFPSstr.length);
        rectY = (int) (positionY - fontSize * 1.2);
        rectX = positionX;

        g.setColor(bgColor);
        g.fillRect(rectX, rectY, rectWidth, rectHeight);

        g.setColor(textColor);
        // g.setFont(font);
        for (String s : newFPSstr) {
            if (s != null) {
                g.drawString(s, positionX, positionY);
                positionY += fontSize * 1.2;
            }
        }
    }

}
