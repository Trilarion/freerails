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
 * MapViewComponent.java
 *
 */
package freerails.client.view;

import freerails.client.common.ModelRootImpl;
import freerails.client.common.ModelRootListener;
import freerails.client.renderer.MapRenderer;
import freerails.client.renderer.RendererRoot;
import freerails.controller.ModelRoot;
import freerails.util.Point2D;
import freerails.world.WorldConstants;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Displays the map, the cursor, and user messages (which are stored on the
 * ModelRoot under the keys QUICK_MESSAGE and PERMANENT_MESSAGE).
 */
public final class MapViewComponentConcrete extends MapViewComponent implements ModelRootListener {

    private static final long serialVersionUID = 3834868087706236208L;
    private static final Font USER_MESSAGE_FONT = new Font("Arial", 0, 12);
    private static final Font LARGE_MESSAGE_FONT = new Font("Arial", 0, 24);
    /**
     * Affects scroll direction and scroll speed relative to the cursor.
     * Examples:
     *
     * 1 := grab map, move 1:1
     *
     * -2 := invert mouse, scroll twice as fast
     */
    private static final int LINEAR_ACCEL = -1;
    // TODO do we really need the robot here, try to do without the robot
    /**
     * A {@link Robot} to compensate mouse cursor movement.
     */
    private static Robot robot;

    static {
        try {
            robot = new Robot();
        } catch (java.awt.AWTException ignored) {
        }
    }

    /**
     * The length of the array is the number of lines. This is necessary since
     * Graphics.drawString(..) doesn't know about newline characters
     */
    private String[] userMessage = new String[0];
    /**
     * Message that will appear in the middle of the screen in
     * {@code LARGE_MESSAGE_FONT}.
     */
    private String message = null;
    /**
     * Time at which to stop displaying the current user message.
     */
    private long displayMessageUntil = 0;
    private FreerailsCursor mapCursor;

    /**
     *
     */
    public MapViewComponentConcrete() {
        super();

        MapViewJComponentMouseAdapter mva = new MapViewJComponentMouseAdapter();
        addMouseListener(mva);
        addMouseMotionListener(mva);
    }

    @Override
    protected void paintComponent(java.awt.Graphics g) {
        if (EventQueue.isDispatchThread()) {
            return;
        }
        super.paintComponent(g);

        if (null != mapCursor && isFocusOwner()) {
            mapCursor.paintCursor(g, new java.awt.Dimension(WorldConstants.TILE_SIZE, WorldConstants.TILE_SIZE));
        }

        if (System.currentTimeMillis() < displayMessageUntil) {
            Rectangle visRect = getVisibleRect();
            g.setColor(Color.WHITE);
            g.setFont(USER_MESSAGE_FONT);

            for (int i = 0; i < userMessage.length; i++) {
                g.drawString(userMessage[i], 50 + visRect.x, 50 + visRect.y + i * 20);
            }
        }

        if (message != null) {
            Rectangle visRect = getVisibleRect();
            g.setColor(Color.lightGray);
            g.setFont(LARGE_MESSAGE_FONT);

            int msgWidth = g.getFontMetrics(LARGE_MESSAGE_FONT).stringWidth(message);
            int msgHeight = g.getFontMetrics(LARGE_MESSAGE_FONT).getHeight();
            g.drawString(message, (int) (visRect.x + (visRect.getWidth() - msgWidth) / 2), (int) (visRect.y + (visRect.getHeight() - msgHeight) / 2));
        }
    }

    /**
     * @param mapRenderer
     * @param modelRoot
     * @param rendererRoot
     * @throws IOException
     */
    public void setup(MapRenderer mapRenderer, ModelRootImpl modelRoot, RendererRoot rendererRoot) throws IOException {
        super.setMapView(mapRenderer);

        setBorder(null);

        mapCursor = new FreerailsCursor(modelRoot, rendererRoot);

        modelRoot.addPropertyChangeListener(this);

    }

    /**
     * @param mapRenderer
     */
    public void setup(MapRenderer mapRenderer) {
        super.setMapView(mapRenderer);
    }

    private void react2curorMove(Point2D newPoint, Point2D oldPoint) {
        float scale = getMapView().getScale();
        Dimension tileSize = new Dimension((int) scale, (int) scale);
        Rectangle vr = getVisibleRect();
        Rectangle rectangleSurroundingCursor = new Rectangle(0, 0, 1, 1);

        rectangleSurroundingCursor.setLocation((newPoint.x - 1) * tileSize.width, (newPoint.y - 1) * tileSize.height);
        rectangleSurroundingCursor.setSize(tileSize.width * 3, tileSize.height * 3);

        if (!(vr.contains(rectangleSurroundingCursor))) {
            int x = newPoint.x * tileSize.width - vr.width / 2;
            int y = newPoint.y * tileSize.height - vr.height / 2;
            scrollRectToVisible(new Rectangle(x, y, vr.width, vr.height));
        }

        repaint((newPoint.x - 1) * tileSize.width, (newPoint.y - 1) * tileSize.height, tileSize.width * 3, tileSize.height * 3);

        repaint((oldPoint.x - 1) * tileSize.width, (oldPoint.y - 1) * tileSize.height, tileSize.width * 3, tileSize.height * 3);
    }

    /**
     * @param g
     * @param tileX
     * @param tileY
     */
    public void paintTile(Graphics g, Point2D p) {
        throw new UnsupportedOperationException();
    }

    /**
     * @param x
     * @param y
     */
    public void refreshTile(Point2D p) {
        throw new UnsupportedOperationException();
    }

    /**
     *
     */
    public void refreshAll() {
        getMapView().refreshAll();
    }

    /**
     * @param g
     * @param visibleRect
     */
    public void paintRect(Graphics g, Rectangle visibleRect) {
        throw new UnsupportedOperationException();
    }

    private void println(String s) {
        StringTokenizer st = new StringTokenizer(s, "\n");
        userMessage = new String[st.countTokens()];

        int i = 0;

        while (st.hasMoreTokens()) {
            userMessage[i] = st.nextToken();
            i++;
        }

        // Display the message for 5 seconds.
        displayMessageUntil = System.currentTimeMillis() + 1000 * 5;
    }

    /**
     * Checks what triggered the specified PropertyChangeEvent and reacts as
     * follows.
     *
     * (1) If it was ModelRoot.CURSOR_POSITION, scrolls the map if necessary.
     *
     *
     * (2) If it was ModelRoot.QUICK_MESSAGE, display or hide the message as
     * appropriate.
     *
     *
     * (3) If it was ModelRoot.PERMANENT_MESSAGE, display or hide the message as
     * appropriate.
     */
    @Override
    public void propertyChange(ModelRoot.Property p, Object oldValue, Object newValue) {

        switch (p) {
            case CURSOR_POSITION:
                Point2D newPoint = (Point2D) newValue;
                Point2D oldPoint = (Point2D) oldValue;

                if (null == oldPoint) {
                    oldPoint = new Point2D();
                }

                react2curorMove(newPoint, oldPoint);
                break;
            case QUICK_MESSAGE:
                String newMessage = (String) newValue;

                if (null != newMessage) {
                    println(newMessage);
                } else {
                    // Its null, so stop displaying whatever we where displaying.
                    displayMessageUntil = Long.MIN_VALUE;
                }
                break;
            case PERMANENT_MESSAGE:
                message = (String) newValue;
                break;
        }
    }

    /**
     * Implements a MouseListener for FreerailsCursor-movement (left mouse
     * button) and a MouseMotionListener for map-scrolling (right mouse button).
     *
     * Possible enhancements: setCursor(blankCursor),
     * g.draw(cursorimage,lastMouseLocation.x,lastMouseLocation.y,null)
     */
    private final class MapViewJComponentMouseAdapter extends MouseInputAdapter {
        /**
         * Screen location of the mouse cursor, when the second mouse button was
         * pressed.
         */
        private final java.awt.Point screenLocation = new java.awt.Point();

        private final java.awt.Point lastMouseLocation = new java.awt.Point();

        /**
         * A variable to sum up relative mouse movement.
         */
        private final java.awt.Point sigmadelta = new java.awt.Point();

        /**
         * Where to scroll - Reflects granularity, scroll direction and
         * acceleration, respects bounds.
         */
        private final java.awt.Point tiledelta = new java.awt.Point();

        @Override
        public void mousePressed(MouseEvent e) {
            /*
             * Note, moving the cursor using the mouse is now handled in
             * UserInputOnMapController
             */
            if (SwingUtilities.isRightMouseButton(e)) {
                setCursor(Cursor.getPredefinedCursor((LINEAR_ACCEL > 0) ? Cursor.HAND_CURSOR : Cursor.MOVE_CURSOR));
                lastMouseLocation.x = e.getX();
                lastMouseLocation.y = e.getY();
                screenLocation.x = e.getX();
                screenLocation.y = e.getY();
                sigmadelta.x = 0;
                sigmadelta.y = 0;
                javax.swing.SwingUtilities.convertPointToScreen(screenLocation, MapViewComponentConcrete.this);
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)) {
                sigmadelta.x += e.getX() - lastMouseLocation.x;
                sigmadelta.y += e.getY() - lastMouseLocation.y;

                int tileSize = (int) getScale();
                /*
      Affects the granularity of the map scrolling (the map is scrolled in
      tileSize/GRANULARITY intervals). Multiply this value with LINEAR_ACCEL to
      be independent of acceleration.
     */
                int GRANULARITY = 2 * LINEAR_ACCEL;
                tiledelta.x = (sigmadelta.x * GRANULARITY) / tileSize;
                tiledelta.y = (sigmadelta.y * GRANULARITY) / tileSize;
                tiledelta.x = ((tiledelta.x * tileSize) / GRANULARITY) * LINEAR_ACCEL;
                tiledelta.y = ((tiledelta.y * tileSize) / GRANULARITY) * LINEAR_ACCEL;

                Rectangle vr = getVisibleRect();
                Rectangle bounds = getBounds();

                int temp; // respect bounds

                if ((temp = vr.x - tiledelta.x) < 0) {
                    sigmadelta.x += temp / LINEAR_ACCEL;
                    tiledelta.x += temp;
                } else if ((temp = (bounds.width) - (vr.x + vr.width) + tiledelta.x) < 0) {
                    sigmadelta.x -= temp / LINEAR_ACCEL;
                    tiledelta.x -= temp;
                }

                if ((temp = vr.y - tiledelta.y) < 0) {
                    sigmadelta.y += temp / LINEAR_ACCEL;
                    tiledelta.y += temp;
                } else if ((temp = (bounds.height) - (vr.y + vr.height) + tiledelta.y) < 0) {
                    sigmadelta.y -= temp / LINEAR_ACCEL;
                    tiledelta.y -= temp;
                }

                if (tiledelta.x != 0 || tiledelta.y != 0) {
                    vr.x -= tiledelta.x;
                    vr.y -= tiledelta.y;
                    scrollRectToVisible(vr);

                    sigmadelta.x -= tiledelta.x / LINEAR_ACCEL;
                    sigmadelta.y -= tiledelta.y / LINEAR_ACCEL;
                    lastMouseLocation.x -= tiledelta.x;
                    lastMouseLocation.y -= tiledelta.y;
                }

                MapViewComponentConcrete.robot.mouseMove(screenLocation.x, screenLocation.y);
            }
        }
    }

}