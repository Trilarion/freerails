/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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

package freerails.util.ui;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

/**
 * Since there is no setMinimum size method on JFrame, we use an instance of
 * this class to do the job.
 */
public class JFrameMinimumSizeEnforcer implements ComponentListener {

    private final int minWidth;
    private final int minHeight;

    /**
     * Creates a listener that enforces the specified minimum dimensions.
     *
     * @param w minimum width in pixels
     * @param h minimum height in pixels
     */
    public JFrameMinimumSizeEnforcer(int w, int h) {
        minHeight = h;
        minWidth = w;
    }

    /**
     * Resizes the component if necessary to meet the configured minimum size.
     *
     * @param e component resize event
     */
    @Override
    public void componentResized(ComponentEvent e) {
        Component c = e.getComponent();

        int width = c.getWidth();
        int height = c.getHeight();

        // we check if either the width
        // or the height are below minimum
        boolean resize = false;

        if (width < minWidth) {
            resize = true;
            width = minWidth;
        }

        if (height < minHeight) {
            resize = true;
            height = minHeight;
        }

        if (resize) {
            c.setSize(width, height);
        }
    }

    /**
     * Ignores component movement because it does not affect the minimum size.
     *
     * @param e component movement event
     */
    @Override
    public void componentMoved(ComponentEvent e) {
    }

    /**
     * Ignores component visibility changes.
     *
     * @param e component visibility event
     */
    @Override
    public void componentShown(ComponentEvent e) {
    }

    /**
     * Ignores component visibility changes.
     *
     * @param e component visibility event
     */
    @Override
    public void componentHidden(ComponentEvent e) {
    }
}