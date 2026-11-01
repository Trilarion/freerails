/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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

package freerails.client.launcher;

import freerails.client.ClientConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;

/**
 * Handles going into fullscreen mode and setting buffer strategy etc.
 */
public class ScreenHandler {

    private static final GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

    public final JFrame frame;
    private final int mode;
    private BufferStrategy bufferStrategy;
    private boolean isInUse = false;
    /**
     * Whether the window is minimised.
     */
    private boolean isMinimised = false;

    /**
     * Creates a screen handler instance.
     *
     * @param f the f parameter
     * @param mode the mode parameter
     */
    public ScreenHandler(JFrame f, int mode) {
        frame = f;
        this.mode = mode;
    }

    /**
     * Sets the repaint off and disable double buffering.
     * @param c the c
     */
    private static void setRepaintOffAndDisableDoubleBuffering(Component c) {
        c.setIgnoreRepaint(true);

        // Since we are using a buffer strategy we don't want Swing
        // to double buffer any JComponents.
        if (c instanceof JComponent jComponent) {
            jComponent.setDoubleBuffered(false);
        }

        if (c instanceof Container) {
            Component[] children = ((Container) c).getComponents();

            for (Component aChildren : children) {
                setRepaintOffAndDisableDoubleBuffering(aChildren);
            }
        }
    }

    /**
     * Performs the exit full screen mode operation.
     */
    public static synchronized void exitFullScreenMode() {
        device.setFullScreenWindow(null);
    }

    /**
     * Performs the apply operation.
     */
    public synchronized void apply() {
        switch (mode) {
            case ClientConstants.FULL_SCREEN: {
                setRepaintOffAndDisableDoubleBuffering(frame);

                /*
                 * We need to make the frame not displayable before calling
                 * setUndecorated(true) otherwise a
                 * IllegalComponentStateException will get thrown.
                 */
                if (frame.isDisplayable()) {
                    frame.dispose();
                }

                frame.setUndecorated(true);
                device.setFullScreenWindow(frame);
                frame.validate();

                break;
            }

            case ClientConstants.WINDOWED_MODE: {
                GraphicsConfiguration configuration = device.getDefaultConfiguration();
                Rectangle screenBounds = configuration.getBounds();
                Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(configuration);
                int screenX = screenBounds.x + screenInsets.left;
                int screenY = screenBounds.y + screenInsets.top;
                int screenWidth = Math.max(1, screenBounds.width - screenInsets.left - screenInsets.right);
                int screenHeight = Math.max(1, screenBounds.height - screenInsets.top - screenInsets.bottom);
                int minimumWidth = Math.min(640, screenWidth);
                int minimumHeight = Math.min(480, screenHeight);
                int width = Math.clamp(minimumWidth, (int) (screenWidth * 0.7), screenWidth);
                int height = Math.clamp(minimumHeight, (int) (screenHeight * 0.7), screenHeight);

                frame.setMinimumSize(new Dimension(minimumWidth, minimumHeight));
                frame.setSize(width, height);
                frame.setLocation(screenX + (screenWidth - width) / 2, screenY + (screenHeight - height) / 2);
                frame.setVisible(true);

                break;
            }

            default:
                throw new IllegalArgumentException(String.valueOf(mode));
        }

        createBufferStrategy();

        frame.addComponentListener(new ComponentAdapter() {
            /**
             * Component resized.
             * @param e the e
             */
            @Override
            public void componentResized(ComponentEvent e) {
                createBufferStrategy();
            }
        });

        frame.addWindowListener(new WindowAdapter() {
            /**
             * Window iconified.
             * @param e the e
             */
            @Override
            public void windowIconified(WindowEvent e) {
                isMinimised = true;
            }

            /**
             * Window deiconified.
             * @param e the e
             */
            @Override
            public void windowDeiconified(WindowEvent e) {
                isMinimised = false;
            }
        });

        isInUse = true;
    }

    /**
     * Creates the buffer strategy.
     */
    private synchronized void createBufferStrategy() {
        // Use 2 back buffers to avoid using too much VRAM.
        frame.createBufferStrategy(2);
        bufferStrategy = frame.getBufferStrategy();
        setRepaintOffAndDisableDoubleBuffering(frame);
    }

    /**
     * Returns the draw graphics.
     *
     * @return the draw graphics produced by this method
     */
    public synchronized Graphics getDrawGraphics() {
        return bufferStrategy.getDrawGraphics();
    }

    /**
     * Performs the swap screens operation.
     */
    public synchronized void swapScreens() {
        if (!bufferStrategy.contentsLost()) {
            bufferStrategy.show();
        }
    }

    /**
     * Returns whether minimised.
     *
     * @return {@code true} if minimised; otherwise, {@code false}
     */
    public synchronized boolean isMinimised() {
        return isMinimised;
    }

    /**
     * Returns whether in use.
     *
     * @return {@code true} if in use; otherwise, {@code false}
     */
    public synchronized boolean isInUse() {
        return isInUse;
    }

    /**
     * Performs the contents restored operation.
     *
     * @return {@code true} if contents restored; otherwise, {@code false}
     */
    public boolean contentsRestored() {
        return bufferStrategy.contentsRestored();
    }
}