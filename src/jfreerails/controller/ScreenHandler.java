package jfreerails.controller;

import java.awt.Component;
import java.awt.Container;
import java.awt.DisplayMode;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JFrame;


/**
 * Handles going into fullscreen mode and setting buffer strategy etc.
 * 
 * @author Luke
 */
final public class ScreenHandler {
	private static final Logger logger = Logger.getLogger(ScreenHandler.class
			.getName());

	public static final int FULL_SCREEN = 0;

	public static final int WINDOWED_MODE = 1;

	public static final int FIXED_SIZE_WINDOWED_MODE = 2;

	public final JFrame frame;

	private BufferStrategy bufferStrategy;

	private DisplayMode displayMode;

	private final int mode;

	private boolean isInUse = false;

	/** Whether the window is minimised. */
	private boolean isMinimised = false;
	
	static GraphicsDevice device = GraphicsEnvironment
	.getLocalGraphicsEnvironment().getDefaultScreenDevice();

	public ScreenHandler(JFrame f, int mode, DisplayMode displayMode) {
		this.displayMode = displayMode;
		frame = f;
		this.mode = mode;
	}

	public ScreenHandler(JFrame f, int mode) {
		frame = f;
		this.mode = mode;
	}

	private static void goFullScreen(JFrame frame, DisplayMode displayMode) {
		
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

			logger.info("Setting display mode to:  "
					+ (new MyDisplayMode(displayMode).toString()));
			device.setDisplayMode(displayMode);
		}

		frame.validate();
	}

	public void apply() {
		switch (mode) {
		case FULL_SCREEN: {
			goFullScreen(frame, displayMode);

			break;
		}

		case WINDOWED_MODE: {
			// Some of the dialogue boxes do not get layed out properly if they
			// are smaller than their
			// minimum size. JFrameMinimumSizeEnforcer increases the size of the
			// Jframe when its size falls
			// below the specified size.
			frame.addComponentListener(new JFrameMinimumSizeEnforcer(640, 480));

			frame.setSize(640, 480);
			frame.setVisible(true);

			break;
		}

		case FIXED_SIZE_WINDOWED_MODE: {
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
			public void componentResized(java.awt.event.ComponentEvent evt) {
				createBufferStrategy();
			}
		});

		frame.addWindowListener(new WindowAdapter() {
			public void windowIconified(WindowEvent e) {
				isMinimised = true;
			}

			public void windowDeiconified(WindowEvent e) {
				isMinimised = false;
			}
		});
		isInUse = true;
	}

	private void createBufferStrategy() {
		// Use 2 backbuffers to avoid using too much VRAM.
		frame.createBufferStrategy(2);
		bufferStrategy = frame.getBufferStrategy();
		setRepaintOffAndDisableDoubleBuffering(frame);
	}

	public Graphics getDrawGraphics() {
		return bufferStrategy.getDrawGraphics();
	}

	public void swapScreens() {
		if (!bufferStrategy.contentsLost()) {
			bufferStrategy.show();
		}
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

			for (int i = 0; i < children.length; i++) {
				setRepaintOffAndDisableDoubleBuffering(children[i]);
			}
		}
	}

	private static DisplayMode getBestDisplayMode() {
		for (int x = 0; x < BEST_DISPLAY_MODES.length; x++) {
			DisplayMode[] modes = device.getDisplayModes();

			for (int i = 0; i < modes.length; i++) {
				if (modes[i].getWidth() == BEST_DISPLAY_MODES[x].getWidth()
						&& modes[i].getHeight() == BEST_DISPLAY_MODES[x]
								.getHeight()
						&& modes[i].getBitDepth() == BEST_DISPLAY_MODES[x]
								.getBitDepth()) {
					logger.fine("Best display mode is "
							+ (new MyDisplayMode(BEST_DISPLAY_MODES[x]))
									.toString());

					return BEST_DISPLAY_MODES[x];
				}
			}
		}

		return null;
	}

	private static final DisplayMode[] BEST_DISPLAY_MODES = new DisplayMode[] {
			new DisplayMode(640, 400, 8, 60),
			new DisplayMode(800, 600, 16, 60),
			new DisplayMode(1024, 768, 8, 60),
			new DisplayMode(1024, 768, 16, 60), };

	public boolean isMinimised() {
		return isMinimised;
	}

	public boolean isInUse() {
		return isInUse;
	}
	
	public static void exitFullScreenMode(){
		device.setFullScreenWindow(null);
	}
}