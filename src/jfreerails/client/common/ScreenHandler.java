package jfreerails.client.common;

import java.awt.Component;
import java.awt.Container;
import java.awt.DisplayMode;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferStrategy;
import java.awt.event.*;

import javax.swing.JComponent;
import javax.swing.JFrame;

/**
 * @version 	1.0
 */
final public class ScreenHandler {

	public static final int FULL_SCREEN = 0;

	public static final int WINDOWED_MODE = 1;

	public static final int FIXED_SIZE_WINDOWED_MODE = 2;

	public final JFrame frame;

	BufferStrategy bufferStrategy;

	DisplayMode displayMode;

	/** Whether the window is minimised */
	private boolean isMinimised = false;

	public ScreenHandler(JFrame f, int mode, DisplayMode displayMode) {
		this.displayMode = displayMode;
		frame = f;
		apply(f, mode);
	}

	public ScreenHandler(JFrame f, int mode) {
		frame = f;
		apply(f, mode);
	}

	public static void goFullScreen(JFrame frame, DisplayMode displayMode) {
		GraphicsDevice device =
			GraphicsEnvironment
				.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice();
		setRepaintOffAndDisableDoubleBuffering(frame);

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

	public void apply(JFrame f, int mode) {
		switch (mode) {

			case FULL_SCREEN :
				{
					goFullScreen(f, displayMode);
					break;
				}
			case WINDOWED_MODE :
				{
					//Some of the dialogue boxes do not get layed out properly if they are smaller than their
					//minimum size.  JFrameMinimumSizeEnforcer increases the size of the Jframe when its size falls
					//below the specified size.
					frame.addComponentListener(
						new JFrameMinimumSizeEnforcer(640, 450));

					frame.setSize(640, 450);
					frame.show();
					break;
				}
			case FIXED_SIZE_WINDOWED_MODE :
				{
					/* We need to make the frame not displayable before calling
					* setUndecorated(true) otherwise a java.awt.IllegalComponentStateException
					* will get thrown.
					*/
					if (frame.isDisplayable()) {
						frame.dispose();
					}
					frame.setUndecorated(true);
					frame.setResizable(false);
					frame.setSize(640, 480);
					frame.show();
					break;

				}
			default :
				{
					throw new IllegalArgumentException(String.valueOf(mode));
				}
		}

		createBufferStrategy();

		f.addComponentListener(new java.awt.event.ComponentAdapter() {
			public void componentResized(java.awt.event.ComponentEvent evt) {
				createBufferStrategy();
			}
		});

		f.addWindowListener(new WindowAdapter() {
			public void windowIconified(WindowEvent e) {
				isMinimised = true;
			}

			public void windowDeiconified(WindowEvent e) {
				isMinimised = false;
			}
		});
	}

	private void createBufferStrategy() {
		//Use 2 backbuffers to avoid using too much VRAM.
		frame.createBufferStrategy(2);
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
					&& modes[i].getBitDepth()
						== BEST_DISPLAY_MODES[x].getBitDepth()) {
					return BEST_DISPLAY_MODES[x];

				}
			}
		}
		return null;
	}

	private static final DisplayMode[] BEST_DISPLAY_MODES =
		new DisplayMode[] {
			new DisplayMode(640, 400, 8, 0),
			new DisplayMode(800, 600, 16, 0),
			new DisplayMode(1024, 768, 8, 0),
			new DisplayMode(1024, 768, 16, 0),
			};

	public boolean isMinimised() {
		return isMinimised;
	}
}
