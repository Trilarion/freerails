/*
 * Copyright (C) 2001 Luke Lindsay
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

/*
 * MapViewJComponent.java
 *
 * Created on 31 July 2001, 13:56
 */
package org.railz.client.view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
// import java.util.StringTokenizer;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import org.railz.controller.MoveReceiver;
import org.railz.client.model.CursorEvent;
import org.railz.client.model.CursorEventListener;
import org.railz.client.model.ModelRoot;
import org.railz.client.top.*;
import org.railz.client.top.NonAuthoritativeMoveExecuter.PendingQueue;
import org.railz.client.common.FPSCounter;
import org.railz.client.common.Stats;
import org.railz.client.renderer.MapRenderer;
import org.railz.world.top.ReadOnlyWorld;


/**
 *
 * @author  Luke Lindsay
 *
 */
final public class MapViewJComponentConcrete extends MapViewJComponent
    implements CursorEventListener {
	private ModelRoot modelRoot;
	private MoveReceiver moveReceiver;
	private StationPlacementCursor stationPlacementCursor;
	private UserInputOnMapController userInputOnMapController;
	private StationTypesPopup stationTypesPopup;
	private static final Font USER_MESSAGE_FONT = new Font("Arial", 0, 12);
	private GUIRoot guiRoot;
    
	private Stats paintStats = new Stats("MapViewJComponent paint");
	private boolean frameRate;
	private boolean debugMoves;

	private JTextArea textArea;
	private int textAreaWidth = -1;
	private int textAreaHeight = -1;

	/** The length of the array is the number of lines.  
	 * This is necessary since Graphics.drawString(..)  doesn't know about
	 * newline characters*/
//	private String[] userMessage = new String[0];
	private LinkedList messages = new LinkedList();
	private class UserMessage {
	    public final String message;
	    public final long createdOn;

	    public UserMessage(String m) {
		message = m;
		createdOn = System.currentTimeMillis();
	    }
	}

	/** Time at which to stop displaying the current user message. */
//	private long displayMessageUntil = 0;
	private FreerailsCursor mapCursor;
	private FPSCounter fpsCounter = new FPSCounter();

	/**
	 * Affects scroll direction and scroll speed relative to the cursor.
	 * Examples:<p>
	 *            1 := grab map, move 1:1<p>
	 *           -2 := invert mouse, scroll twice as fast
	 */
	private final int LINEAR_ACCEL = -1;

	/** Affects the granularity of the map scrolling (the map is scrolled
	 * in tileSize/GRANULARITY intervals). Multiply this value with
	 * LINEAR_ACCEL to be independent of acceleration.
	 */
	private final int GRANULARITY = 2 * LINEAR_ACCEL;

	/**
	 * A {@link Robot} to compensate mouse cursor movement
	 */
	private static Robot robot;

	static {
	    try {
		robot = new Robot();
	    } catch (java.awt.AWTException e) {
	    }
	}

	private ActionListener debugMovesListener = new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		debugMoves = modelRoot.getDebugModel().
		    getClientMoveDebugModel().isSelected();
	    }
	};

	private ActionListener frameRateListener = new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		frameRate = modelRoot.getDebugModel().
		    getFrameRateDebugModel().isSelected();
	    }
	};

	/**
	 * Implements a MouseListener for FreerailsCursor-movement (left
	 * mouse button) and a MouseMotionListener for map-scrolling (right
	 * mouse button).<p>
	 * Possible enhancements:
	 *     setCursor(blankCursor),
	 *     g.draw(cursorimage,lastMouseLocation.x,lastMouseLocation.y,null)
	 */
	final private class MapViewJComponentMouseAdapter extends
	    MouseInputAdapter {
		/**
		 * Screen location of the mouse cursor, when the second mouse
		 * button was pressed
		 */
		private Point screenLocation = new Point();
		private Point lastMouseLocation = new Point();

		/**
		 * A variable to sum up relative mouse movement
		 */
		private Point sigmadelta = new Point();

		/**
		 * Where to scroll - Reflects granularity, scroll direction
		 * and acceleration, respects bounds.
		 */
		private Point tiledelta = new Point();

		public void mousePressed(MouseEvent evt) {
			
		    if (SwingUtilities.isLeftMouseButton(evt)) {
				int x = evt.getX();
				int y = evt.getY();
				float scale = getScale();
				Dimension tileSize = new Dimension((int)scale,
					(int)scale);
				mapCursor.tryMoveCursor(new Point(x / tileSize.width,
					    y / tileSize.height));
				MapViewJComponentConcrete.this.requestFocus();
		    }

		    if (SwingUtilities.isRightMouseButton(evt)) {
				MapViewJComponentConcrete.this.setCursor(Cursor.getPredefinedCursor((LINEAR_ACCEL > 0)
					    ? Cursor.HAND_CURSOR : Cursor.MOVE_CURSOR));
				lastMouseLocation.x = evt.getX();
				lastMouseLocation.y = evt.getY();
				screenLocation.x = evt.getX();
				screenLocation.y = evt.getY();
				sigmadelta.x = 0;
				sigmadelta.y = 0;
				javax.swing.SwingUtilities.convertPointToScreen(screenLocation,
					MapViewJComponentConcrete.this);
		    }
		}

		public void mouseReleased(MouseEvent evt) {
		    MapViewJComponentConcrete.this.setCursor
			(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}

		public void mouseDragged(MouseEvent evt) {
		    if (SwingUtilities.isRightMouseButton(evt)) {
			sigmadelta.x += evt.getX() - lastMouseLocation.x;
			sigmadelta.y += evt.getY() - lastMouseLocation.y;

			int tileSize = (int)getScale();
			tiledelta.x = (int)(sigmadelta.x * GRANULARITY) /
			    tileSize;
			tiledelta.y = (int)(sigmadelta.y * GRANULARITY) /
			    tileSize;
			tiledelta.x = (int)((tiledelta.x * tileSize) /
				GRANULARITY) * LINEAR_ACCEL;
			tiledelta.y = (int)((tiledelta.y * tileSize) /
				GRANULARITY) * LINEAR_ACCEL;

			Rectangle vr =
			    MapViewJComponentConcrete.this.getVisibleRect();
			Rectangle bounds =
			    MapViewJComponentConcrete.this.getBounds();

			int temp; //respect bounds

			if ((temp = vr.x - tiledelta.x) < 0) {
			    sigmadelta.x += temp / LINEAR_ACCEL;
			    tiledelta.x += temp;
			} else if ((temp = (bounds.width) - (vr.x + vr.width) +
				    tiledelta.x) < 0) {
			    sigmadelta.x -= temp / LINEAR_ACCEL;
			    tiledelta.x -= temp;
			}

			if ((temp = vr.y - tiledelta.y) < 0) {
			    sigmadelta.y += temp / LINEAR_ACCEL;
			    tiledelta.y += temp;
			} else if ((temp = (bounds.height) - (vr.y +
					vr.height) + tiledelta.y) < 0) {
			    sigmadelta.y -= temp / LINEAR_ACCEL;
			    tiledelta.y -= temp;
			}

			if (tiledelta.x != 0 || tiledelta.y != 0) {
			    vr.x -= tiledelta.x;
			    vr.y -= tiledelta.y;
			    MapViewJComponentConcrete.this.scrollRectToVisible
				(vr);

			    sigmadelta.x -= tiledelta.x / LINEAR_ACCEL;
			    sigmadelta.y -= tiledelta.y / LINEAR_ACCEL;
			    lastMouseLocation.x -= tiledelta.x;
			    lastMouseLocation.y -= tiledelta.y;
			}

			MapViewJComponentConcrete.robot.mouseMove
			    (screenLocation.x, screenLocation.y);
		    }
		}
	    }

	protected void paintComponent(java.awt.Graphics g) {
	    /*
	     * Swing Javadoc states that we shouldn't leave the Graphics
	     * context in an altered state, so we make a copy here instead
	     */
	    Graphics myGraphics = g.create();
	    paintStats.enter();
	    super.paintComponent(myGraphics);
	    if (null != mapCursor) {
		mapCursor.cursorRenderer.paintCursor(myGraphics,
			new java.awt.Dimension(30, 30));
	    }

	    /* display any user messages */
	    long now = System.currentTimeMillis();
	    UserMessage m;
	    // remove all older than 5 seconds
	    while (!messages.isEmpty() &&
		    ((UserMessage) messages.get(0)).createdOn + 5000 < now)
		messages.removeFirst();

	    Rectangle visRect = this.getVisibleRect();
	    synchronized (messages) {
		if (! messages.isEmpty()) {
		    if (textAreaWidth != visRect.width ||
			    textAreaHeight != visRect.height) {
			textAreaWidth = visRect.width;
			textAreaHeight = visRect.height;
			textArea.setSize(textAreaWidth, textAreaHeight);
		    }
		    StringBuffer buf = new StringBuffer();
		    for (int i = 0; i < messages.size() ; i++) {
			buf.append(((UserMessage) messages.get(i)).message);
			buf.append("\n");
		    }
		    textArea.setText(buf.toString());
		    myGraphics.translate(visRect.x, visRect.y);
		    textArea.paint(myGraphics);
		    myGraphics.translate(-visRect.x, -visRect.y);
		}
	    }
	//	    myGraphics.drawString(this.userMessage[i], 10+visRect.x,
	//		    10+visRect.y+i*20);
	    paintStats.exit();
	    
	    if (frameRate || debugMoves)
		myGraphics.translate(visRect.x, visRect.y);
	    if (frameRate)
		fpsCounter.updateFPSCounter(myGraphics);
	    if (debugMoves) {
		myGraphics.setColor(Color.WHITE);
		myGraphics.fillRect(50, 50, 50, 20);
		myGraphics.setColor(Color.BLACK);
		myGraphics.drawString(String.valueOf
			(((ConnectionAdapter) modelRoot.getReceiver())
			 .getNumBlockedMoves()), 50, 65);
	    }

	    /* dispose of the graphics context */	
	    myGraphics.dispose();
	}

	public MapViewJComponentConcrete() {
	    super();

	    MapViewJComponentMouseAdapter mva = new
		MapViewJComponentMouseAdapter();
	    this.addMouseListener(mva);
	    this.addMouseMotionListener(mva);
	    stationTypesPopup = new StationTypesPopup();
	    textArea = new JTextArea();
	    textArea.setFont(USER_MESSAGE_FONT);
	    textArea.setLineWrap(true);
	    textArea.setWrapStyleWord(true);
	    textArea.setForeground(Color.WHITE);
	    textArea.setMargin(new java.awt.Insets(10, 10, 10, 10));
	    textArea.setOpaque(false);
	    textArea.setVisible(true);

	    setDoubleBuffered(true);
	}

	public void setup(GUIRoot gr, ModelRoot mr) {
	    DetailMapView mv = new DetailMapView(mr);
	    super.setMapView(mv);
	    guiRoot = gr;
	    this.setBorder(null);
	    this.removeKeyListener(this.mapCursor);
	    this.mapCursor = new FreerailsCursor(mv);
	    mapCursor.addCursorEventListener(this);
	    this.addKeyListener(mapCursor);
	    userInputOnMapController = new UserInputOnMapController(mr);
	    moveReceiver = new MapViewMoveReceiver(mv);
	    mr.getMoveChainFork().addSplitMoveReceiver(moveReceiver);
	    mr.setCursor(mapCursor);
	    gr.getDialogueBoxController().setDefaultFocusOwner(this);
	    stationPlacementCursor = new StationPlacementCursor(mr,
		    mv.getStationRadius(), this);
	    stationTypesPopup.setup(mr, mv.getStationRadius());
	    userInputOnMapController.setup(gr, this, stationTypesPopup);
	    mr.setUserMessageLogger(this);
	    gr.setMapViewJComponent(this);
	    setIgnoreRepaint(true);

	    if (modelRoot != null) {
		modelRoot.getDebugModel().getFrameRateDebugModel().
		    removeActionListener(frameRateListener);
		modelRoot.getDebugModel().getClientMoveDebugModel().
		    removeActionListener(debugMovesListener);
	    }

	    modelRoot = mr;
	    frameRate = modelRoot.getDebugModel().getFrameRateDebugModel().
		isSelected();
	    debugMoves = modelRoot.getDebugModel().getClientMoveDebugModel().
		isSelected();
	    modelRoot.getDebugModel().getFrameRateDebugModel().
		addActionListener(frameRateListener);
	    modelRoot.getDebugModel().getClientMoveDebugModel().
		addActionListener(debugMovesListener);
	}

	public void setup(MapRenderer mv) {
	    super.setMapView(mv);
	}

	public void cursorJumped(CursorEvent ce) {
	    reactToCursorMovement(ce);
	}

	public void cursorOneTileMove(CursorEvent ce) {
	    reactToCursorMovement(ce);
	}

	public void cursorKeyPressed(CursorEvent ce) {
	    reactToCursorMovement(ce);
	}

	private void reactToCursorMovement(CursorEvent ce) {
	    float scale = getMapView().getScale();
	    Dimension tileSize = new Dimension((int)scale, (int)scale);
	    Rectangle vr = this.getVisibleRect();
	    Rectangle rectangleSurroundingCursor = new Rectangle(0, 0, 1, 1);
	    rectangleSurroundingCursor.setLocation((ce.newPosition.x - 1) *
		    tileSize.width, (ce.newPosition.y - 1) * tileSize.height);
	    rectangleSurroundingCursor.setSize(tileSize.width * 3,
		    tileSize.height * 3);

	    if (!(vr.contains(rectangleSurroundingCursor))) {
		int x = ce.newPosition.x * tileSize.width - vr.width / 2;
		int y = ce.newPosition.y * tileSize.height - vr.height / 2;
		this.scrollRectToVisible(new Rectangle(x, y, vr.width,
			    vr.height));
	    }

	    this.repaint((ce.newPosition.x - 1) * tileSize.width,
		    (ce.newPosition.y - 1) * tileSize.height, tileSize.width *
		    3, tileSize.height * 3);
	    this.repaint((ce.oldPosition.x - 1) * tileSize.width,
		    (ce.oldPosition.y - 1) * tileSize.height, tileSize.width *
		    3, tileSize.height * 3);
	}

	public void paintTile(Graphics g, int tileX, int tileY) {
	}

	public void refreshTile(int x, int y) {
	}

	public void paintRect(Graphics g, Rectangle visibleRect) {
	}

	public FreerailsCursor getMapCursor() {
	    return mapCursor;
	}

	public void println(String s) {
	    synchronized (messages) {
		messages.add(new UserMessage(s));
	    }
	}

	public void doFrameUpdate(Graphics g) {
	    Rectangle visRect = getVisibleRect();
	    g.translate(-visRect.x, -visRect.y);
	    paintComponent(g);
	    g.translate(visRect.x, visRect.y);
	}
    }
