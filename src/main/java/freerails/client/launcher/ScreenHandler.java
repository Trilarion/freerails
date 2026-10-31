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
import freerails.util.Vec2D;
import org.apache.log4j.Logger;

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
    private static final Logger logger = Logger.getLogger(ScreenHandler.class.getName());
    private static final DisplayMode[] BEST_DISPLAY_MODES = new DisplayMode[]{new DisplayMode(640, 400, 8, 60), new DisplayMode(800, 600, 16, 60), new DisplayMode(1024, 768, 8, 60), new DisplayMode(1024, 768, 16, 60),};

    public final JFrame frame;
    private final int mode;
    private BufferStrategy bufferStrategy;
    private Vec2D displayMode;
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
     * @param displayMode the map coordinate
     */
    public ScreenHandler(JFrame f, int mode, Vec2D displayMode) {
        this.displayMode = displayMode;
        frame = f;
        this.mode = mode;
    }

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
     * Returns the best display mode.
     * @return the best display mode
     */
    private static Vec2D getBestDisplayMode() {
        for (DisplayMode BEST_DISPLAY_MODE : BEST_DISPLAY_MODES) {
            DisplayMode[] modes = device.getDisplayModes();

            for (DisplayMode mode1 : modes) {
                if (mode1.getWidth() == BEST_DISPLAY_MODE.getWidth() && mode1.getHeight() == BEST_DISPLAY_MODE.getHeight() && mode1.getBitDepth() == BEST_DISPLAY_MODE.getBitDepth()) {
                    return new Vec2D(BEST_DISPLAY_MODE.getWidth(), BEST_DISPLAY_MODE.getHeight());
                }
            }
        }

        return null;
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
                Vec2D displayMode1 = displayMode;

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

                if (device.isDisplayChangeSupported()) {
                    if (null == displayMode1) {
                        displayMode1 = getBestDisplayMode();
                    }

                    logger.info("Setting display mode to:  " + displayMode1.toString());
                    device.setDisplayMode(new DisplayMode(displayMode1.x(), displayMode.y(), DisplayMode.BIT_DEPTH_MULTI, DisplayMode.REFRESH_RATE_UNKNOWN));
                }

                frame.validate();

                break;
            }

            case ClientConstants.WINDOWED_MODE: {
                frame.setMinimumSize(new Dimension(640, 480));
                frame.setSize(640, 480);
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