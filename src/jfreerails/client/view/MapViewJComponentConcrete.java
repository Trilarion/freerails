/*
 * MapViewJComponent.java
 *
 * Created on 31 July 2001, 13:56
 */
package jfreerails.client.view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.MouseEvent;
import java.util.StringTokenizer;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;
import jfreerails.client.common.Stats;
import jfreerails.client.renderer.MapRenderer;
import jfreerails.world.top.ReadOnlyWorld;


/**
 *
 * @author  Luke Lindsay
 *
 */
final public class MapViewJComponentConcrete extends MapViewJComponent
    implements CursorEventListener {
    private static final Font USER_MESSAGE_FONT = new Font("Arial", 0, 12);
    private static final Font LARGE_MESSAGE_FONT = new Font("Arial", 0, 24);
    private Stats paintStats = new Stats("MapViewJComponent paint");

    /** The length of the array is the number of lines.
     * This is necessary since Graphics.drawString(..)  doesn't know about newline characters*/
    private String[] userMessage = new String[0];

    /**
     * Message that will appear in the middle of the screen in <code>LARGE_MESSAGE_FONT</code>.
     */
    private String message = null;

    /** Time at which to stop displaying the current user message. */
    private long displayMessageUntil = 0;
    private FreerailsCursor mapCursor;

    /**
    * Affects scroll direction and scroll speed relative to the cursor.
    * Examples:<p>
    *            1 := grab map, move 1:1<p>
    *           -2 := invert mouse, scroll twice as fast
    */
    private final int LINEAR_ACCEL = -1;

    /**
    * Affects the granularity of the map scrolling (the map is scrolled in
    * tileSize/GRANULARITY intervals). Multiply this value with LINEAR_ACCEL to
    * be independent of acceleration.
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

    /**
    * Implements a MouseListener for FreerailsCursor-movement (left
    * mouse button) and a MouseMotionListener for map-scrolling (right
    * mouse button).<p>
    * Possible enhancements:
    *     setCursor(blankCursor),
    *     g.draw(cursorimage,lastMouseLocation.x,lastMouseLocation.y,null)
    */
    final private class MapViewJComponentMouseAdapter extends MouseInputAdapter {
        /**
        * Screen location of the mouse cursor, when the second mouse button was
        * pressed
        */
        private Point screenLocation = new Point();
        private Point lastMouseLocation = new Point();

        /**
        * A variable to sum up relative mouse movement
        */
        private Point sigmadelta = new Point();

        /**
        * Where to scroll - Reflects granularity, scroll direction and
        * acceleration, respects bounds.
        */
        private Point tiledelta = new Point();

        public void mousePressed(MouseEvent evt) {
            if (SwingUtilities.isLeftMouseButton(evt)) {
                int x = evt.getX();
                int y = evt.getY();
                float scale = getScale();
                Dimension tileSize = new Dimension((int)scale, (int)scale);
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
            MapViewJComponentConcrete.this.setCursor(Cursor.getPredefinedCursor(
                    Cursor.DEFAULT_CURSOR));
        }

        public void mouseDragged(MouseEvent evt) {
            if (SwingUtilities.isRightMouseButton(evt)) {
                sigmadelta.x += evt.getX() - lastMouseLocation.x;
                sigmadelta.y += evt.getY() - lastMouseLocation.y;

                int tileSize = (int)getScale();
                tiledelta.x = (int)(sigmadelta.x * GRANULARITY) / tileSize;
                tiledelta.y = (int)(sigmadelta.y * GRANULARITY) / tileSize;
                tiledelta.x = (int)((tiledelta.x * tileSize) / GRANULARITY) * LINEAR_ACCEL;
                tiledelta.y = (int)((tiledelta.y * tileSize) / GRANULARITY) * LINEAR_ACCEL;

                Rectangle vr = MapViewJComponentConcrete.this.getVisibleRect();
                Rectangle bounds = MapViewJComponentConcrete.this.getBounds();

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
                } else if ((temp = (bounds.height) - (vr.y + vr.height) +
                            tiledelta.y) < 0) {
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

    /*
    final private class MapViewJComponentMouseAdapter
            extends java.awt.event.MouseAdapter {

            public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
                    if (SwingUtilities.isLeftMouseButton(mouseEvent)) {
                            int x = mouseEvent.getX();
                            int y = mouseEvent.getY();
                            float scale = mapView.getScale();
                            Dimension tileSize = new Dimension((int) scale, (int) scale);
                            cursor.TryMoveCursor(
                                    new java.awt.Point(
                                            x / tileSize.width,
                                            y / tileSize.height));
                            MapViewJComponentConcrete.this.requestFocus();
                    }
            }
    }
    */
    protected void paintComponent(java.awt.Graphics g) {
        paintStats.enter();
        super.paintComponent(g);

        /* no need to do this again
                java.awt.Graphics2D g2 = (java.awt.Graphics2D)g;

                java.awt.Rectangle r = this.getVisibleRect();

                mapView.paintRect(g2, r);
        */
        if (null != mapCursor) {
            mapCursor.cursorRenderer.paintCursor(g,
                new java.awt.Dimension(30, 30));
        }

        if (System.currentTimeMillis() < this.displayMessageUntil) {
            Rectangle visRect = this.getVisibleRect();
            g.setColor(Color.WHITE);
            g.setFont(USER_MESSAGE_FONT);

            for (int i = 0; i < userMessage.length; i++) {
                g.drawString(this.userMessage[i], 50 + visRect.x,
                    50 + visRect.y + i * 20);
            }
        }

        if (message != null) {
            Rectangle visRect = this.getVisibleRect();
            g.setColor(Color.lightGray);
            g.setFont(LARGE_MESSAGE_FONT);

            int msgWidth = g.getFontMetrics(LARGE_MESSAGE_FONT).stringWidth(message);
            int msgHeight = g.getFontMetrics(LARGE_MESSAGE_FONT).getHeight();
            g.drawString(message,
                (int)(visRect.x + (visRect.getWidth() - msgWidth) / 2),
                (int)(visRect.y + (visRect.getHeight() - msgHeight) / 2));
        }

        paintStats.exit();
    }

    public MapViewJComponentConcrete() {
        super();

        MapViewJComponentMouseAdapter mva = new MapViewJComponentMouseAdapter();
        this.addMouseListener(mva);
        this.addMouseMotionListener(mva);
    }

    public void setup(MapRenderer mv, ReadOnlyWorld w) {
        super.setMapView(mv);

        this.setBorder(null);

        this.removeKeyListener(this.mapCursor);

        this.mapCursor = new FreerailsCursor(mv);

        mapCursor.addCursorEventListener(this);

        this.addKeyListener(mapCursor);
    }

    public void setup(MapRenderer mv) {
        super.setMapView(mv);
    }

    public void cursorJumped(CursorEvent ce) {
        //repaintMap(ce);
        reactToCursorMovement(ce);
    }

    /* The map is repainted in reponse to moves being received
     using the class MapViewMoveReceiver.

    public void repaintMap(CursorEvent ce) {

            Point tile = new Point();
            for (tile.x = ce.newPosition.x - 1;
                    tile.x < ce.newPosition.x + 2;
                    tile.x++) {
                    for (tile.y = ce.newPosition.y - 1;
                            tile.y < ce.newPosition.y + 2;
                            tile.y++) {
                            mapView.refreshTile(tile.x, tile.y);
                    }
            }
    }
    */
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
        rectangleSurroundingCursor.setLocation((ce.newPosition.x - 1) * tileSize.width,
            (ce.newPosition.y - 1) * tileSize.height);
        rectangleSurroundingCursor.setSize(tileSize.width * 3,
            tileSize.height * 3);

        if (!(vr.contains(rectangleSurroundingCursor))) {
            int x = ce.newPosition.x * tileSize.width - vr.width / 2;
            int y = ce.newPosition.y * tileSize.height - vr.height / 2;
            this.scrollRectToVisible(new Rectangle(x, y, vr.width, vr.height));
        }

        this.repaint((ce.newPosition.x - 1) * tileSize.width,
            (ce.newPosition.y - 1) * tileSize.height, tileSize.width * 3,
            tileSize.height * 3);
        this.repaint((ce.oldPosition.x - 1) * tileSize.width,
            (ce.oldPosition.y - 1) * tileSize.height, tileSize.width * 3,
            tileSize.height * 3);
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
        StringTokenizer st = new StringTokenizer(s, "\n");
        this.userMessage = new String[st.countTokens()];

        int i = 0;

        while (st.hasMoreTokens()) {
            userMessage[i] = st.nextToken();
            i++;
        }

        //Display the message for 5 seconds.
        displayMessageUntil = System.currentTimeMillis() + 1000 * 5;
    }

    public void showMessage(String message) {
        this.message = message;
    }

    public void hideMessage() {
        message = null;
    }
}