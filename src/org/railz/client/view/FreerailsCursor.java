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

/**
*
* Created on 01 August 2001, 06:02
*/
package org.railz.client.view;

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

import org.railz.client.model.MapCursor;
import org.railz.client.model.CursorEvent;
import org.railz.client.model.CursorEventListener;
import org.railz.client.renderer.MapRenderer;
import org.railz.world.common.*;

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
            moveCursor(CompassPoints.SOUTHWEST);

            break;

        case KeyEvent.VK_NUMPAD2:
            moveCursor(CompassPoints.SOUTH);

            break;

        case KeyEvent.VK_NUMPAD3:
            moveCursor(CompassPoints.SOUTHEAST);

            break;

        case KeyEvent.VK_NUMPAD4:
            moveCursor(CompassPoints.WEST);

            break;

        case KeyEvent.VK_NUMPAD6:
            moveCursor(CompassPoints.EAST);

            break;

        case KeyEvent.VK_NUMPAD7:
            moveCursor(CompassPoints.NORTHWEST);

            break;

        case KeyEvent.VK_NUMPAD8:
            moveCursor(CompassPoints.NORTH);

            break;

        case KeyEvent.VK_NUMPAD9:
            moveCursor(CompassPoints.NORTHEAST);

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
            if ((deltaX != 0 || deltaY != 0) &&
		    deltaX >= -1 && deltaX <= 1 &&
		    deltaY >= -1 && deltaY <= 1) {
		fireOffCursorOneTileMove
		    (CompassPoints.unitDeltasToDirection(deltaX,
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
    * @param mv The view that the curors moves across.
    */
    public FreerailsCursor(MapRenderer mv) {
        this.mapView = mv;
    }

    public void addCursorEventListener(CursorEventListener l) {
        listeners.addElement(l);
    }

    public void removeCursorEventListener(CursorEventListener l) {
	listeners.removeElement(l);
    }

    private void moveCursor(byte v) {
        tryMoveCursor(new Point(cursorMapPosition.x +
		    CompassPoints.getUnitDeltaX(v),
                cursorMapPosition.y + CompassPoints.getUnitDeltaY(v)));
    }

    private void fireOffCursorJumped(Point oldPosition, Point newPosition) {
        CursorEvent ce = new CursorEvent(this);
        ce.oldPosition = oldCursorMapPosition;
        ce.newPosition = newPosition;

        for (int i = 0; i < listeners.size(); i++) {
            ((CursorEventListener)listeners.elementAt(i)).cursorJumped(ce);
        }
    }

    private void fireOffCursorOneTileMove(byte v, Point oldPosition) {
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
