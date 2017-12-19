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

package experimental;

import freerails.client.top.GameLoop;
import freerails.controller.ScreenHandler;

import javax.swing.*;
import java.awt.*;

/**
 * This class tests that the game loop and screen handler are working correctly.
 * All it does is display the current time in ms and display the number of
 * frames per second.
 */
public class AnimationExpt extends JComponent {

    private static final long serialVersionUID = 3690191057862473264L;

    /**
     * @param args
     */
    public static void main(String[] args) {
        System.setProperty("SHOWFPS", "true");

        JFrame f = new JFrame();
        f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        f.getContentPane().add(new AnimationExpt());

        ScreenHandler screenHandler = new ScreenHandler(f, ScreenHandler.WINDOWED_MODE);
        screenHandler.apply();

        GameLoop gameLoop = new GameLoop(screenHandler);
        Thread t = new Thread(gameLoop);
        t.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        long l = System.currentTimeMillis();
        String str = String.valueOf(l);
        g.drawString(str, 100, 100);
    }
}