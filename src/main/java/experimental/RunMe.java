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

/*
 * RunMe.java
 *
 * Created on 23 June 2002, 02:44
 */
package experimental;

import freerails.client.top.GameLoop;
import freerails.controller.ScreenHandler;

import javax.swing.*;

/**
 * Tests that ClientJFrame and ScreenHandler work together.
 *
 */
public class RunMe {

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        JFrame jFrame = new freerails.client.top.ClientJFrame(
                new SimpleComponentFactoryImpl2());

        // jFrame.show();
        ScreenHandler screenHandler = new ScreenHandler(jFrame,
                ScreenHandler.WINDOWED_MODE);
        GameLoop gameLoop = new GameLoop(screenHandler);
        Thread t = new Thread(gameLoop);
        t.start();
    }
}