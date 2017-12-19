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

package freerails.client.top;

import java.awt.*;
import java.util.LinkedList;

/**
 * Provides a method that draws a String showing the average FPS over the last
 * complete 5000ms interval.
 */
public class FPScounter {

    private static final int MAX_FPS_VALUES = 100;

    private final LinkedList<Double> fpsValues;
    private final int fontSize;
    private final Color bgColor;
    private final String[] newFPSstr;
    private int newFrameCount = 0;
    private long lastFrameTime;
    private double mean;

    FPScounter() {
        fpsValues = new LinkedList<>();
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
