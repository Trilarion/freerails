/**
*
* Created on 01 August 2001, 06:02
*/
package jfreerails.client.view;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.util.Vector;
import jfreerails.client.renderer.MapRenderer;
import jfreerails.world.common.OneTileMoveVector;


final public class FreerailsCursor implements KeyListener, MapCursor {
    public CursorRenderer cursorRenderer = new CursorRenderer();

    /**
     *  CursorEventListener
     */
    protected Vector listeners = new Vector();
    private Point oldCursorMapPosition = new Point(0, 0);
    private int blinkValue = 1;

    /*The cursor tile and one tile in each direction
    */
    private BasicStroke stroke = new BasicStroke(3);
    private Point cursorMapPosition = new Point(0, 0);
    private MapRenderer mapView;
    private String message = null;

    /** This inner class controls rendering of the cursor.
    */
    final public class CursorRenderer {
        /** Paints the cursor.  The method calculates position to paint it based on the
        * tile size and the cursor's map position.
        * @param g The graphics object to paint the cursor on.
        * @param tileSize The dimensions of a tile.
        */
        public void paintCursor(Graphics g, Dimension tileSize) {
            Graphics2D g2 = (Graphics2D)g;

            //First draw the cursor.
            g2.setStroke(stroke);

            if (1 == blinkValue) {
                g2.setColor(java.awt.Color.white); //The colour of the cursor
            } else {
                g2.setColor(java.awt.Color.black);
            }

            int x = cursorMapPosition.x * tileSize.width;
            int y = cursorMapPosition.y * tileSize.height;
            g2.drawRect(x, y, tileSize.width, tileSize.height);

            //Second, draw a message below the cursor if appropriate.
            if (null != message && !message.equals("")) {
                int fontSize = 12;
                Font font = new Font("Arial", 0, fontSize);
                FontRenderContext frc = g2.getFontRenderContext();
                TextLayout layout = new TextLayout(message, font, frc);

                //We want the message to be centered below the cursor.
                float visibleAdvance = layout.getVisibleAdvance();
                float textX = (float)(x + (tileSize.width / 2) -
                    (visibleAdvance / 2));
                float textY = y + tileSize.height + fontSize + 5;
                g.setColor(java.awt.Color.white);
                layout.draw(g2, textX, textY);
            }
        }
    }

    /** Use this method rather than KeyTyped to process keyboard input.
    * @param keyEvent The key pressed.
    */
    public void keyPressed(KeyEvent keyEvent) {
        switch (keyEvent.getKeyCode()) {
        case KeyEvent.VK_NUMPAD1:
            moveCursor(OneTileMoveVector.SOUTH_WEST);

            break;

        case KeyEvent.VK_NUMPAD2:
            moveCursor(OneTileMoveVector.SOUTH);

            break;

        case KeyEvent.VK_NUMPAD3:
            moveCursor(OneTileMoveVector.SOUTH_EAST);

            break;

        case KeyEvent.VK_NUMPAD4:
            moveCursor(OneTileMoveVector.WEST);

            break;

        case KeyEvent.VK_NUMPAD6:
            moveCursor(OneTileMoveVector.EAST);

            break;

        case KeyEvent.VK_NUMPAD7:
            moveCursor(OneTileMoveVector.NORTH_WEST);

            break;

        case KeyEvent.VK_NUMPAD8:
            moveCursor(OneTileMoveVector.NORTH);

            break;

        case KeyEvent.VK_NUMPAD9:
            moveCursor(OneTileMoveVector.NORTH_EAST);

            break;

        case KeyEvent.VK_ENTER:default:
            fireOffCursorKeyPressed(keyEvent, cursorMapPosition);
        }
    }

    public void tryMoveCursor(Point tryThisPoint) {
        message = null;

        float tileSize = mapView.getScale();
        Dimension mapSizeInPixels = mapView.getMapSizeInPixels();
        int maxX = (int)(mapSizeInPixels.width / tileSize) - 2;
        int maxY = (int)(mapSizeInPixels.height / tileSize) - 2;
        Rectangle legalRectangle; //The set of legal cursor positions.
        legalRectangle = new Rectangle(1, 1, maxX, maxY);

        if (legalRectangle.contains(tryThisPoint)) {
            /*Move the cursor. */
            oldCursorMapPosition.setLocation(cursorMapPosition);
            cursorMapPosition.setLocation(tryThisPoint);

            int deltaX = cursorMapPosition.x - oldCursorMapPosition.x;
            int deltaY = cursorMapPosition.y - oldCursorMapPosition.y;

            /*Build track! */
            if (OneTileMoveVector.checkValidity(deltaX, deltaY)) {
                fireOffCursorOneTileMove(OneTileMoveVector.getInstance(deltaX,
                        deltaY), oldCursorMapPosition);
            } else {
                fireOffCursorJumped(oldCursorMapPosition, cursorMapPosition);
            }
        } else {
            this.message = "Illegal cursor position!";
        }
    }

    /** Empty method, needed to implement the KeyListener interface.
    * @param keyEvent The key typed.
    */
    public void keyTyped(KeyEvent keyEvent) {
    }

    /** Use keyPressed instead of this method.
    * @param keyEvent the key pressed
    */
    public void keyReleased(KeyEvent keyEvent) {
    }

    /** Creates a new FreerailsCursor.
    * @param mapView The view that the curors moves across.
    */
    public FreerailsCursor(MapRenderer mv) {
        this.mapView = mv;
    }

    public void addCursorEventListener(CursorEventListener l) {
        listeners.addElement(l);
    }

    private void moveCursor(OneTileMoveVector v) {
        tryMoveCursor(new Point(cursorMapPosition.x + v.getDx(),
                cursorMapPosition.y + v.getDy()));
    }

    private void fireOffCursorJumped(Point oldPosition, Point newPosition) {
        CursorEvent ce = new CursorEvent(this);
        ce.oldPosition = oldCursorMapPosition;
        ce.newPosition = newPosition;

        for (int i = 0; i < listeners.size(); i++) {
            ((CursorEventListener)listeners.elementAt(i)).cursorJumped(ce);
        }
    }

    private void fireOffCursorOneTileMove(OneTileMoveVector v, Point oldPosition) {
        CursorEvent ce = new CursorEvent(this);
        ce.vector = v;
        ce.oldPosition = oldPosition;
        ce.newPosition = cursorMapPosition;

        for (int i = 0; i < listeners.size(); i++) {
            ((CursorEventListener)listeners.elementAt(i)).cursorOneTileMove(ce);
        }
    }

    private void fireOffCursorKeyPressed(KeyEvent keyEvent, Point position) {
        CursorEvent ce = new CursorEvent(this);
        ce.keyEvent = keyEvent;
        ce.oldPosition = position;
        ce.newPosition = position;

        for (int i = 0; i < listeners.size(); i++) {
            ((CursorEventListener)listeners.elementAt(i)).cursorKeyPressed(ce);
        }
    }   

    public void setMessage(String message) {
        this.message = message;
    }
}