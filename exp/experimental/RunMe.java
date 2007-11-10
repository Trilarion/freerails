/*
 * RunMe.java
 *
 * Created on 23 June 2002, 02:44
 */
package experimental;

import javax.swing.JFrame;

import jfreerails.client.top.GameLoop;
import jfreerails.controller.ScreenHandler;

/**
 * Tests that ClientJFrame and ScreenHandler work together.
 * 
 * @author Luke Lindsay
 */
public class RunMe {
    public static void main(String[] args) {
        JFrame jFrame = new jfreerails.client.top.ClientJFrame(
                new SimpleComponentFactoryImpl2());

        // jFrame.show();
        ScreenHandler screenHandler = new ScreenHandler(jFrame,
                ScreenHandler.WINDOWED_MODE);
        GameLoop gameLoop = new GameLoop(screenHandler);
        Thread t = new Thread(gameLoop);
        t.start();
    }
}