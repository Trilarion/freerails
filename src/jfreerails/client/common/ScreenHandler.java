package jfreerails.client.common;

import java.awt.Component;
import java.awt.Container;
import java.awt.DisplayMode;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferStrategy;

import javax.swing.JComponent;
import javax.swing.JFrame;


/**
 * @version 	1.0
 */
final public class ScreenHandler {

	final JFrame frame;

	BufferStrategy bufferStrategy;

	public ScreenHandler(JFrame f, boolean fullscreen) {

		frame = f;

		if (fullscreen) {
			GraphicsDevice device =
				GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			setRepaintOffAndDisableDoubleBuffering(frame);
			
			/* We need to make the frame not displayable before calling
			 * setUndecorated(true) otherwise a java.awt.IllegalComponentStateException
			 * will get thrown.
			 */
			if(frame.isDisplayable()){
				frame.dispose();
			}
			frame.setUndecorated(true);
			device.setFullScreenWindow(frame);
			if (device.isDisplayChangeSupported()) {
				chooseBestDisplayMode(device);
			}
			frame.validate();
		} else {
			//Some of the dialogue boxes do not get layed out properly if they are smaller than their
			//minimum size.  JFrameMinimumSizeEnforcer increases the size of the Jframe when its size falls
			//below the specified size.
			frame.addComponentListener(new JFrameMinimumSizeEnforcer(640, 450));
			
			
			frame.setSize(640, 450);
			frame.show();
		}

		createBufferStrategy();

		f.addComponentListener(new java.awt.event.ComponentAdapter() {
			public void componentResized(java.awt.event.ComponentEvent evt) {
				createBufferStrategy();
			}
		});
	}

	private void createBufferStrategy() {
		frame.createBufferStrategy(3);
		bufferStrategy = frame.getBufferStrategy();

		setRepaintOffAndDisableDoubleBuffering(frame);
		
	}

	public Graphics getDrawGraphics() {
		return bufferStrategy.getDrawGraphics();
	}

	public boolean swapScreens() {
		boolean done = false;
		if (!bufferStrategy.contentsLost()) {
			bufferStrategy.show();
			done = true;
		}
		return done;
	}

	public static void setRepaintOffAndDisableDoubleBuffering(Component c) {

		c.setIgnoreRepaint(true);

		//Since we are using a buffer strategy we don't want Swing
		//to double buffer any JComponents.
		if (c instanceof JComponent) {
			JComponent jComponent = (JComponent) c;
			jComponent.setDoubleBuffered(false);
		}

		if (c instanceof java.awt.Container) {
			Component[] children = ((Container) c).getComponents();
			for (int i = 0; i < children.length; i++) {
				setRepaintOffAndDisableDoubleBuffering(children[i]);
			}
		}
	}

	private static DisplayMode getBestDisplayMode(GraphicsDevice device) {
		for (int x = 0; x < BEST_DISPLAY_MODES.length; x++) {
			DisplayMode[] modes = device.getDisplayModes();
			for (int i = 0; i < modes.length; i++) {
				if (modes[i].getWidth() == BEST_DISPLAY_MODES[x].getWidth()
					&& modes[i].getHeight() == BEST_DISPLAY_MODES[x].getHeight()
					&& modes[i].getBitDepth() == BEST_DISPLAY_MODES[x].getBitDepth()) {
					return BEST_DISPLAY_MODES[x];

				}
			}
		}
		return null;
	}

	private static void chooseBestDisplayMode(GraphicsDevice device) {
		DisplayMode best = getBestDisplayMode(device);
		if (best != null) {
			device.setDisplayMode(best);
		}

	}

	private static final DisplayMode[] BEST_DISPLAY_MODES =
		new DisplayMode[] {
			new DisplayMode(800, 600, 16, 0),
			new DisplayMode(1024, 768, 8, 0),
			new DisplayMode(640, 400, 8, 0),			
			new DisplayMode(1024, 768, 16, 0),
			
			};

}
