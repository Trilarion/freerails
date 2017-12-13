/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.railz.client.common;

import java.awt.Component;
import java.awt.DisplayMode;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.event.*;
import java.awt.BufferCapabilities;
import java.awt.ImageCapabilities;
import java.awt.image.BufferStrategy;
import java.awt.image.VolatileImage;
import java.util.logging.*;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;

/**
 * This class manages screen resolution changes, and is the entry point for
 * synchronous frame updates.
 *
 * <h1>Screen redraw operation</h1>
 * Screen redraws may be caused by the following events:
 * <h2>periodic frame update</h2>
 * This occurs when the update() method is called. The main map display is
 * redrawn, plus any components which may be obscuring it.
 * <h2>A refresh is caused by the event thread (eg. the user clicks a
 * button)</h2>
 * Client code should cause the relevant parts to be redrawn.
 * <h2>A refresh is caused by a world model change</h2>
 * Client code should call repaint() for the changed components.
 *
 * <p>Note that redraws are inefficient at the moment due to differing platform
 * behaviour, e.g. OS X provides native double buffering so buffering in Java
 * is not required. X11 redraws should be double buffered by Java, but there is
 * as yet no mechanism to sync the two buffers without doing a full redraw
 * which is wasteful.
 */
final public class ScreenHandler {
    public static final int NUM_BUFFERS = 2;
    public static final int FULL_SCREEN = 0;
    public static final int WINDOWED_MODE = 1;
    public final JFrame frame;
    DisplayMode displayMode;
    private UpdatedComponent updatedComponent;
    private boolean isVolatile;
    private int oldWidth;
    private int oldHeight;
    private BufferStrategy bufferStrategy;
    private boolean runningOSX = "Mac OS X".equals
	(System.getProperty("os.name"));
    private static final Logger logger = Logger.getLogger("global");

    /**
     * Game starts off in WINDOWED_MODE by default
     */
    private int currentMode = WINDOWED_MODE;

    /** Whether the window is minimised */
    private boolean isMinimised = false;

    public ScreenHandler(JFrame f, UpdatedComponent uc,
	    int mode, DisplayMode displayMode) {
	updatedComponent = uc;
        this.displayMode = displayMode;
        frame = f;
        apply(f, mode);
    }

    public ScreenHandler(JFrame f, int mode) {
        frame = f;
        apply(f, mode);
    }

    /**
     * @return one of FULL_SCREEN, WINDOWED_MODE
     */
    public int getMode() {
	return currentMode;
    }

    private static void goFullScreen(JFrame frame, DisplayMode displayMode) {
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment()
                                                   .getDefaultScreenDevice();

        /* We need to make the frame not displayable before calling
         * setUndecorated(true) otherwise a java.awt.IllegalComponentStateException
         * will get thrown.
         */
        if (frame.isDisplayable()) {
            frame.dispose();
        }

        frame.setUndecorated(true);
        device.setFullScreenWindow(frame);

        if (device.isDisplayChangeSupported()) {
            if (null == displayMode) {
                displayMode = getBestDisplayMode(device);
            }

            if (null != displayMode) {
                device.setDisplayMode(displayMode);
            }
        }

        frame.validate();
    }

    private void apply(JFrame f, int mode) {
        switch (mode) {
	    case FULL_SCREEN:
            goFullScreen(f, displayMode);
            break;
        case WINDOWED_MODE:
            break;
        default:
            throw new IllegalArgumentException(String.valueOf(mode));
        }

        f.addWindowListener(new WindowAdapter() {
                public void windowIconified(WindowEvent e) {
                    isMinimised = true;
                }

                public void windowDeiconified(WindowEvent e) {
                    isMinimised = false;
                }
            });

	currentMode = mode;
    }

    private static DisplayMode getBestDisplayMode(GraphicsDevice device) {
        for (int x = 0; x < BEST_DISPLAY_MODES.length; x++) {
            DisplayMode[] modes = device.getDisplayModes();

            for (int i = 0; i < modes.length; i++) {
                if (modes[i].getWidth() == BEST_DISPLAY_MODES[x].getWidth() &&
                        modes[i].getHeight() == BEST_DISPLAY_MODES[x].getHeight() &&
                        modes[i].getBitDepth() == BEST_DISPLAY_MODES[x].getBitDepth()) {
                    return BEST_DISPLAY_MODES[x];
                }
            }
        }

        return null;
    }

    private static final DisplayMode[] BEST_DISPLAY_MODES = new DisplayMode[] {
            new DisplayMode(640, 400, 8, 0), new DisplayMode(800, 600, 16, 0),
            new DisplayMode(1024, 768, 8, 0), new DisplayMode(1024, 768, 16, 0),
        };

    public boolean isMinimised() {
        return isMinimised;
    }
    
    /**
     * Update the display. This draws 1 "frame".
     */
    public void update() {
	try {
	    SwingUtilities.invokeAndWait(swingWorker);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    private void markLayerDirty(JLayeredPane lp, int layer) {
	Component dirtyComponents[] = lp.getComponentsInLayer(layer);
	for (int i = 0; i < dirtyComponents.length; i++) {
	    if (dirtyComponents[i] instanceof JComponent) {
		RepaintManager.currentManager(dirtyComponents[i]).
		    markCompletelyDirty((JComponent) dirtyComponents[i]);
	    }
	}
    }

    private Runnable swingWorker = new Runnable() {
	public void run() {
	    if (isMinimised || ! frame.isVisible()) {
		return;
	    }
	    if (bufferStrategy == null) {
		frame.createBufferStrategy(2);
		BufferStrategy bs = frame.getBufferStrategy();
		logger.log(Level.INFO, "Buffer capabilities:");
		BufferCapabilities bc = bs.getCapabilities();
		logger.log(Level.INFO, "isFullScreenRequired = " +
		       	bc.isFullScreenRequired());
		logger.log(Level.INFO, "isPageFlipping = " +
		       	bc.isPageFlipping());
		ImageCapabilities ic = bc.getBackBufferCapabilities();
		logger.log(Level.INFO, "isAccelerated = " + ic.isAccelerated());
		logger.log(Level.INFO, "isTrueVolatile = " +
		       	ic.isTrueVolatile());
		logger.log(Level.INFO, "isMultiBufferAvailable = " +
			bc.isMultiBufferAvailable());
		isVolatile = ic.isTrueVolatile();
		bufferStrategy = bs;
	    }

	    bufferStrategy = frame.getBufferStrategy();
	    /* OS X rendering appears to be different */
	    if (runningOSX) {
		do {
		    /* mark everything in the layers above as being dirty */
		    JLayeredPane lp = frame.getLayeredPane();
		    markLayerDirty(lp, lp.DRAG_LAYER.intValue());
		    markLayerDirty(lp, lp.MODAL_LAYER.intValue());
		    markLayerDirty(lp, lp.PALETTE_LAYER.intValue());
		    markLayerDirty(lp, lp.POPUP_LAYER.intValue());

		    Graphics g = bufferStrategy.getDrawGraphics();
		    Insets i = frame.getInsets();
		    g.translate(i.left, i.top);
		    updatedComponent.doFrameUpdate(g);
		    /* draw everything in the layers above the main layer */
		    RepaintManager.currentManager(frame).paintDirtyRegions();

		    g.dispose();
		} while (bufferStrategy.contentsLost());
		bufferStrategy.show();
		return;
	    }

	    /* non-OS X rendering code */
            int w = frame.getWidth();
            int h = frame.getHeight();
            if (w != oldWidth || h != oldHeight) {
                oldWidth = w;
                oldHeight = h;
                frame.createBufferStrategy(NUM_BUFFERS);
                bufferStrategy = frame.getBufferStrategy();
            }
	    do {
		/* draw the whole JFrame */
		Graphics g = bufferStrategy.getDrawGraphics();
		frame.paint(g);
		g.dispose();
	    } while (bufferStrategy.contentsLost());
	    bufferStrategy.show();
	    return;
	}
    };

    public boolean isVolatile() {
	return isVolatile;
    }

    public Component showDialog(Component c, String title) {
	switch (getMode()) {
	    case ScreenHandler.FULL_SCREEN:
		JInternalFrame jif = new JInternalFrame(title,
			true);
		jif.getContentPane().add(c);
		jif.pack();
		jif.setLocation((frame.getWidth() - jif.getWidth()) / 2,
			(frame.getHeight() - jif.getHeight()) / 2);

		frame.getLayeredPane().add(jif, JLayeredPane.MODAL_LAYER);
		jif.show();
		return jif;
	    default:
		JDialog jd = new JDialog(frame, title, false);
		jd.getContentPane().add(c);
		jd.pack();
		jd.setLocation(frame.getX() +
			(frame.getWidth() - jd.getWidth()) / 2,
		       	frame.getY() +
			(frame.getHeight() - jd.getHeight()) / 2);
		jd.show();
		return jd;
	}
    }
}
