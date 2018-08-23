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

package freerails.client.renderer.map.detail;

import freerails.client.ClientConstants;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * Implements a MouseListener for FreerailsCursor-movement (left mouse
 * button) and a MouseMotionListener for map-scrolling (right mouse button).
 *
 * Possible enhancements: setCursor(blankCursor),
 * g.draw(cursorimage,lastMouseLocation.x,lastMouseLocation.y,null)
 */
final class DetailMapViewComponentMouseAdapter extends MouseInputAdapter {

    /**
     * A {@link Robot} to compensate mouse cursor movement.
     */
    private static Robot robot;
    static {
        try {
            robot = new Robot();
        } catch (AWTException ignored) {
        }
    }

    private DetailMapViewComponentConcrete detailMapViewComponentConcrete;
    /**
     * Screen location of the mouse cursor, when the second mouse button was
     * pressed.
     */
    private final Point screenLocation = new Point();

    private final Point lastMouseLocation = new Point();

    /**
     * A variable to sum up relative mouse movement.
     */
    private final Point sigmadelta = new Point();

    /**
     * Where to scroll - Reflects granularity, scroll direction and
     * acceleration, respects bounds.
     */
    private final Point tiledelta = new Point();

    public DetailMapViewComponentMouseAdapter(DetailMapViewComponentConcrete detailMapViewComponentConcrete) {
        this.detailMapViewComponentConcrete = detailMapViewComponentConcrete;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        /*
         * Note, moving the cursor using the mouse is now handled in
         * UserInputOnMapController
         */
        if (SwingUtilities.isRightMouseButton(e)) {
            detailMapViewComponentConcrete.setCursor(Cursor.getPredefinedCursor(ClientConstants.MAP_SCROLL_SPEED > 0 ? Cursor.HAND_CURSOR : Cursor.MOVE_CURSOR));
            lastMouseLocation.x = e.getX();
            lastMouseLocation.y = e.getY();
            screenLocation.x = e.getX();
            screenLocation.y = e.getY();
            sigmadelta.x = 0;
            sigmadelta.y = 0;
            SwingUtilities.convertPointToScreen(screenLocation, detailMapViewComponentConcrete);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        detailMapViewComponentConcrete.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            sigmadelta.x += e.getX() - lastMouseLocation.x;
            sigmadelta.y += e.getY() - lastMouseLocation.y;

            int tileSize = (int) detailMapViewComponentConcrete.getScale();
            /*  Affects the granularity of the map scrolling (the map is scrolled in
                tileSize/GRANULARITY intervals). Multiply this value with LINEAR_ACCEL to
                be independent of acceleration.
             */
            int GRANULARITY = 2 * ClientConstants.MAP_SCROLL_SPEED;
            tiledelta.x = sigmadelta.x * GRANULARITY / tileSize;
            tiledelta.y = sigmadelta.y * GRANULARITY / tileSize;
            tiledelta.x = (tiledelta.x * tileSize / GRANULARITY) * ClientConstants.MAP_SCROLL_SPEED;
            tiledelta.y = (tiledelta.y * tileSize / GRANULARITY) * ClientConstants.MAP_SCROLL_SPEED;

            Rectangle vr = detailMapViewComponentConcrete.getVisibleRect();
            Rectangle bounds = detailMapViewComponentConcrete.getBounds();

            int temp; // respect bounds

            if ((temp = vr.x - tiledelta.x) < 0) {
                sigmadelta.x += temp / ClientConstants.MAP_SCROLL_SPEED;
                tiledelta.x += temp;
            } else if ((temp = bounds.width - (vr.x + vr.width) + tiledelta.x) < 0) {
                sigmadelta.x -= temp / ClientConstants.MAP_SCROLL_SPEED;
                tiledelta.x -= temp;
            }

            if ((temp = vr.y - tiledelta.y) < 0) {
                sigmadelta.y += temp / ClientConstants.MAP_SCROLL_SPEED;
                tiledelta.y += temp;
            } else if ((temp = bounds.height - (vr.y + vr.height) + tiledelta.y) < 0) {
                sigmadelta.y -= temp / ClientConstants.MAP_SCROLL_SPEED;
                tiledelta.y -= temp;
            }

            if (tiledelta.x != 0 || tiledelta.y != 0) {
                vr.x -= tiledelta.x;
                vr.y -= tiledelta.y;
                detailMapViewComponentConcrete.scrollRectToVisible(vr);

                sigmadelta.x -= tiledelta.x / ClientConstants.MAP_SCROLL_SPEED;
                sigmadelta.y -= tiledelta.y / ClientConstants.MAP_SCROLL_SPEED;
                lastMouseLocation.x -= tiledelta.x;
                lastMouseLocation.y -= tiledelta.y;
            }

            robot.mouseMove(screenLocation.x, screenLocation.y);
        }
    }
}
