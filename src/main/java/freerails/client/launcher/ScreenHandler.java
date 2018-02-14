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

package freerails.client.launcher;

import freerails.client.ClientConfig;
import freerails.controller.DisplayModeWithName;
import freerails.util.ui.JFrameMinimumSizeEnforcer;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;

/**
 * Handles going into fullscreen mode and setting buffer strategy etc.
 */
public class ScreenHandler {

    private static final GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    private static final Logger logger = Logger.getLogger(ScreenHandler.class.getName());
    private static final java.awt.DisplayMode[] BEST_DISPLAY_MODES = new java.awt.DisplayMode[]{new java.awt.DisplayMode(640, 400, 8, 60), new java.awt.DisplayMode(800, 600, 16, 60), new java.awt.DisplayMode(1024, 768, 8, 60), new java.awt.DisplayMode(1024, 768, 16, 60),};

    public final JFrame frame;
    private final int mode;
    private BufferStrategy bufferStrategy;
    private java.awt.DisplayMode displayMode;
    private boolean isInUse = false;
    /**
     * Whether the window is minimised.
     */
    private boolean isMinimised = false;

    /**
     * @param f
     * @param mode
     * @param displayMode
     */
    public ScreenHandler(JFrame f, int mode, java.awt.DisplayMode displayMode) {
        this.displayMode = displayMode;
        frame = f;
        this.mode = mode;
    }

    /**
     * @param f
     * @param mode
     */
    public ScreenHandler(JFrame f, int mode) {
        frame = f;
        this.mode = mode;
    }

    private static void goFullScreen(JFrame frame, java.awt.DisplayMode displayMode) {

        setRepaintOffAndDisableDoubleBuffering(frame);

        /*
         * We need to make the frame not displayable before calling
         * setUndecorated(true) otherwise a
         * java.awt.IllegalComponentStateException will get thrown.
         */
        if (frame.isDisplayable()) {
            frame.dispose();
        }

        frame.setUndecorated(true);
        device.setFullScreenWindow(frame);

        if (device.isDisplayChangeSupported()) {
            if (null == displayMode) {
                displayMode = getBestDisplayMode();
            }

            logger.info("Setting display mode to:  " + (new DisplayModeWithName(displayMode).toString()));
            device.setDisplayMode(displayMode);
        }

        frame.validate();
    }

    private static void setRepaintOffAndDisableDoubleBuffering(Component c) {
        c.setIgnoreRepaint(true);

        // Since we are using a buffer strategy we don't want Swing
        // to double buffer any JComponents.
        if (c instanceof JComponent) {
            JComponent jComponent = (JComponent) c;
            jComponent.setDoubleBuffered(false);
        }

        if (c instanceof java.awt.Container) {
            Component[] children = ((Container) c).getComponents();

            for (Component aChildren : children) {
                setRepaintOffAndDisableDoubleBuffering(aChildren);
            }
        }
    }

    private static java.awt.DisplayMode getBestDisplayMode() {
        for (java.awt.DisplayMode BEST_DISPLAY_MODE : BEST_DISPLAY_MODES) {
            java.awt.DisplayMode[] modes = device.getDisplayModes();

            for (java.awt.DisplayMode mode1 : modes) {
                if (mode1.getWidth() == BEST_DISPLAY_MODE.getWidth() && mode1.getHeight() == BEST_DISPLAY_MODE.getHeight() && mode1.getBitDepth() == BEST_DISPLAY_MODE.getBitDepth()) {
                    logger.debug("Best display mode is " + (new DisplayModeWithName(BEST_DISPLAY_MODE)).toString());

                    return BEST_DISPLAY_MODE;
                }
            }
        }

        return null;
    }

    /**
     *
     */
    public static synchronized void exitFullScreenMode() {
        device.setFullScreenWindow(null);
    }

    /**
     *
     */
    public synchronized void apply() {
        switch (mode) {
            case ClientConfig.FULL_SCREEN: {
                goFullScreen(frame, displayMode);

                break;
            }

            case ClientConfig.WINDOWED_MODE: {
                // Some of the dialogue boxes do not get laid out properly if they
                // are smaller than their
                // minimum size. JFrameMinimumSizeEnforcer increases the size of the
                // Jframe when its size falls
                // below the specified size.
                frame.addComponentListener(new JFrameMinimumSizeEnforcer(640, 480));

                frame.setSize(640, 480);
                frame.setVisible(true);

                break;
            }

            case ClientConfig.FIXED_SIZE_WINDOWED_MODE: {
                /*
                 * We need to make the frame not displayable before calling
                 * setUndecorated(true) otherwise a
                 * java.awt.IllegalComponentStateException will get thrown.
                 */
                if (frame.isDisplayable()) {
                    frame.dispose();
                }

                frame.setUndecorated(true);
                frame.setResizable(false);
                frame.setSize(640, 480);
                frame.setVisible(true);

                break;
            }

            default:
                throw new IllegalArgumentException(String.valueOf(mode));
        }

        createBufferStrategy();

        frame.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                createBufferStrategy();
            }
        });

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowIconified(WindowEvent e) {
                isMinimised = true;
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
                isMinimised = false;
            }
        });

        isInUse = true;
    }

    private synchronized void createBufferStrategy() {
        // Use 2 back buffers to avoid using too much VRAM.
        frame.createBufferStrategy(2);
        bufferStrategy = frame.getBufferStrategy();
        setRepaintOffAndDisableDoubleBuffering(frame);
    }

    /**
     * @return
     */
    public synchronized Graphics getDrawGraphics() {
        return bufferStrategy.getDrawGraphics();
    }

    /**
     *
     */
    public synchronized void swapScreens() {
        if (!bufferStrategy.contentsLost()) {
            bufferStrategy.show();
        }
    }

    /**
     * @return
     */
    public synchronized boolean isMinimised() {
        return isMinimised;
    }

    /**
     * @return
     */
    public synchronized boolean isInUse() {
        return isInUse;
    }

    /**
     * @return
     */
    public boolean contentsRestored() {
        return bufferStrategy.contentsRestored();
    }
}