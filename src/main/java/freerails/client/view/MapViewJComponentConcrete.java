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
 * MapViewJComponent.java
 *
 */
package freerails.client.view;

import freerails.client.ClientConstants;
import freerails.client.common.ModelRootImpl;
import freerails.client.common.ModelRootListener;
import freerails.client.renderer.MapRenderer;
import freerails.client.renderer.RendererRoot;
import freerails.controller.ModelRoot;
import freerails.util.Point2D;

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
public final class MapViewJComponentConcrete extends MapViewJComponent
        implements ModelRootListener {
    private static final long serialVersionUID = 3834868087706236208L;

    private static final Font USER_MESSAGE_FONT = new Font("Arial", 0, 12);

    private static final Font LARGE_MESSAGE_FONT = new Font("Arial", 0, 24);
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
     * Affects scroll direction and scroll speed relative to the cursor.
     * Examples:
     *
     * 1 := grab map, move 1:1
     *
     * -2 := invert mouse, scroll twice as fast
     */
    private final int LINEAR_ACCEL = -1;
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
    public MapViewJComponentConcrete() {
        super();

        MapViewJComponentMouseAdapter mva = new MapViewJComponentMouseAdapter();
        this.addMouseListener(mva);
        this.addMouseMotionListener(mva);
    }

    @Override
    protected void paintComponent(java.awt.Graphics g) {
        if (EventQueue.isDispatchThread()) {
            return;
        }
        super.paintComponent(g);

        if (null != mapCursor && this.isFocusOwner()) {
            mapCursor.paintCursor(g, new java.awt.Dimension(
                    ClientConstants.TILE_SIZE, ClientConstants.TILE_SIZE));
        }

        if (System.currentTimeMillis() < this.displayMessageUntil) {
            Rectangle visRect = this.getVisibleRect();
            g.setColor(Color.WHITE);
            g.setFont(USER_MESSAGE_FONT);

            for (int i = 0; i < userMessage.length; i++) {
                g.drawString(this.userMessage[i], 50 + visRect.x, 50
                        + visRect.y + i * 20);
            }
        }

        if (message != null) {
            Rectangle visRect = this.getVisibleRect();
            g.setColor(Color.lightGray);
            g.setFont(LARGE_MESSAGE_FONT);

            int msgWidth = g.getFontMetrics(LARGE_MESSAGE_FONT).stringWidth(
                    message);
            int msgHeight = g.getFontMetrics(LARGE_MESSAGE_FONT).getHeight();
            g.drawString(message,
                    (int) (visRect.x + (visRect.getWidth() - msgWidth) / 2),
                    (int) (visRect.y + (visRect.getHeight() - msgHeight) / 2));
        }
    }

    /**
     * @param mv
     * @param mr
     * @param rr
     * @throws IOException
     */
    public void setup(MapRenderer mv, ModelRootImpl mr, RendererRoot rr)
            throws IOException {
        super.setMapView(mv);

        this.setBorder(null);

        this.mapCursor = new FreerailsCursor(mr, rr);

        mr.addPropertyChangeListener(this);

    }

    /**
     * @param mv
     */
    public void setup(MapRenderer mv) {
        super.setMapView(mv);
    }

    private void react2curorMove(Point2D newPoint, Point2D oldPoint) {
        float scale = getMapView().getScale();
        Dimension tileSize = new Dimension((int) scale, (int) scale);
        Rectangle vr = this.getVisibleRect();
        Rectangle rectangleSurroundingCursor = new Rectangle(0, 0, 1, 1);

        rectangleSurroundingCursor.setLocation((newPoint.x - 1)
                * tileSize.width, (newPoint.y - 1) * tileSize.height);
        rectangleSurroundingCursor.setSize(tileSize.width * 3,
                tileSize.height * 3);

        if (!(vr.contains(rectangleSurroundingCursor))) {
            int x = newPoint.x * tileSize.width - vr.width / 2;
            int y = newPoint.y * tileSize.height - vr.height / 2;
            this.scrollRectToVisible(new Rectangle(x, y, vr.width, vr.height));
        }

        this.repaint((newPoint.x - 1) * tileSize.width, (newPoint.y - 1)
                * tileSize.height, tileSize.width * 3, tileSize.height * 3);

        this.repaint((oldPoint.x - 1) * tileSize.width, (oldPoint.y - 1)
                * tileSize.height, tileSize.width * 3, tileSize.height * 3);
    }

    /**
     * @param g
     * @param tileX
     * @param tileY
     */
    public void paintTile(Graphics g, int tileX, int tileY) {
        throw new UnsupportedOperationException();
    }

    /**
     * @param x
     * @param y
     */
    public void refreshTile(int x, int y) {
        throw new UnsupportedOperationException();
    }

    /**
     *
     */
    public void refreshAll() {
        this.getMapView().refreshAll();
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
        this.userMessage = new String[st.countTokens()];

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
     *
     * @param p
     * @param after
     * @param before
     */
    @Override
    public void propertyChange(ModelRoot.Property p, Object before, Object after) {

        switch (p) {
            case CURSOR_POSITION:
                Point2D newPoint = (Point2D) after;
                Point2D oldPoint = (Point2D) before;

                if (null == oldPoint) {
                    oldPoint = new Point2D();
                }

                react2curorMove(newPoint, oldPoint);
                break;
            case QUICK_MESSAGE:
                String newMessage = (String) after;

                if (null != newMessage) {
                    println(newMessage);
                } else {
                    // Its null, so stop displaying whatever we where displaying.
                    displayMessageUntil = Long.MIN_VALUE;
                }
                break;
            case PERMANENT_MESSAGE:
                message = (String) after;
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
        public void mousePressed(MouseEvent evt) {
            /*
             * Note, moving the cursor using the mouse is now handled in
             * UserInputOnMapController
             */
            if (SwingUtilities.isRightMouseButton(evt)) {
                MapViewJComponentConcrete.this
                        .setCursor(Cursor
                                .getPredefinedCursor((LINEAR_ACCEL > 0) ? Cursor.HAND_CURSOR
                                        : Cursor.MOVE_CURSOR));
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

        @Override
        public void mouseReleased(MouseEvent evt) {
            MapViewJComponentConcrete.this.setCursor(Cursor
                    .getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }

        @Override
        public void mouseDragged(MouseEvent evt) {
            if (SwingUtilities.isRightMouseButton(evt)) {
                sigmadelta.x += evt.getX() - lastMouseLocation.x;
                sigmadelta.y += evt.getY() - lastMouseLocation.y;

                int tileSize = (int) getScale();
                /*
      Affects the granularity of the map scrolling (the map is scrolled in
      tileSize/GRANULARITY intervals). Multiply this value with LINEAR_ACCEL to
      be independent of acceleration.
     */
                int GRANULARITY = 2 * LINEAR_ACCEL;
                tiledelta.x = (sigmadelta.x * GRANULARITY) / tileSize;
                tiledelta.y = (sigmadelta.y * GRANULARITY) / tileSize;
                tiledelta.x = ((tiledelta.x * tileSize) / GRANULARITY)
                        * LINEAR_ACCEL;
                tiledelta.y = ((tiledelta.y * tileSize) / GRANULARITY)
                        * LINEAR_ACCEL;

                Rectangle vr = MapViewJComponentConcrete.this.getVisibleRect();
                Rectangle bounds = MapViewJComponentConcrete.this.getBounds();

                int temp; // respect bounds

                if ((temp = vr.x - tiledelta.x) < 0) {
                    sigmadelta.x += temp / LINEAR_ACCEL;
                    tiledelta.x += temp;
                } else if ((temp = (bounds.width) - (vr.x + vr.width)
                        + tiledelta.x) < 0) {
                    sigmadelta.x -= temp / LINEAR_ACCEL;
                    tiledelta.x -= temp;
                }

                if ((temp = vr.y - tiledelta.y) < 0) {
                    sigmadelta.y += temp / LINEAR_ACCEL;
                    tiledelta.y += temp;
                } else if ((temp = (bounds.height) - (vr.y + vr.height)
                        + tiledelta.y) < 0) {
                    sigmadelta.y -= temp / LINEAR_ACCEL;
                    tiledelta.y -= temp;
                }

                if (tiledelta.x != 0 || tiledelta.y != 0) {
                    vr.x -= tiledelta.x;
                    vr.y -= tiledelta.y;
                    MapViewJComponentConcrete.this.scrollRectToVisible(vr);

                    sigmadelta.x -= tiledelta.x / LINEAR_ACCEL;
                    sigmadelta.y -= tiledelta.y / LINEAR_ACCEL;
                    lastMouseLocation.x -= tiledelta.x;
                    lastMouseLocation.y -= tiledelta.y;
                }

                MapViewJComponentConcrete.robot.mouseMove(screenLocation.x,
                        screenLocation.y);
            }
        }
    }

}