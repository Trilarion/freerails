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

package freerails.client.view;

import freerails.client.ClientConfig;
import freerails.client.renderer.RendererRoot;
import freerails.controller.BuildMode;
import freerails.controller.ModelRoot;
import freerails.util.Vector2D;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.io.IOException;

/**
 * Paints the cursor on the map, note the cursor's position is stored on the
 * ModelRoot under the key CURSOR_POSITION.
 */
public final class FreerailsCursor {

    private final Image buildTrack, upgradeTrack, removeTrack, infoMode;
    private final ModelRoot modelRoot;

    /**
     * The location of the cursor last time paintCursor(.) was called.
     */
    private Vector2D lastCursorPosition = new Vector2D();

    /**
     * The time in ms the cursor arrived at its current position.
     */
    private long timeArrived = 0;

    /**
     * Creates a new FreerailsCursor.
     */
    public FreerailsCursor(ModelRoot modelRoot, RendererRoot rendererRoot) throws IOException {
        this.modelRoot = modelRoot;
        this.modelRoot.setProperty(ModelRoot.Property.CURSOR_MESSAGE, null);
        buildTrack = rendererRoot.getImage("cursor/buildtrack.png");
        upgradeTrack = rendererRoot.getImage("cursor/upgradetrack.png");
        removeTrack = rendererRoot.getImage("cursor/removetrack.png");
        infoMode = rendererRoot.getImage("cursor/infomode.png");
    }

    /**
     * Paints the cursor. The method calculates position to paint it based on
     * the tile size and the cursor's map position.
     *
     * @param g        The graphics object to paint the cursor on.
     * @param tileSize The dimensions of a tile.
     */
    public void paintCursor(Graphics g, Dimension tileSize) {
        Graphics2D g2 = (Graphics2D) g;

        BuildMode buildMode = (BuildMode) modelRoot.getProperty(ModelRoot.Property.TRACK_BUILDER_MODE);

        Vector2D cursorMapPosition = (Vector2D) modelRoot.getProperty(ModelRoot.Property.CURSOR_POSITION);

        // Has the cursor moved since we last painted it?
        if (!cursorMapPosition.equals(lastCursorPosition)) {
            lastCursorPosition = cursorMapPosition;
            timeArrived = System.currentTimeMillis();
        }

        int x = cursorMapPosition.x * tileSize.width;
        int y = cursorMapPosition.y * tileSize.height;

        Image cursor = null;
        switch (buildMode) {
            case BUILD_TRACK:
                cursor = buildTrack;
                break;
            case REMOVE_TRACK:
                cursor = removeTrack;
                break;
            case UPGRADE_TRACK:
                cursor = upgradeTrack;
                break;
            case IGNORE_TRACK:
                cursor = infoMode;
                break;
            case BUILD_STATION:
                cursor = buildTrack;
                break;
        }

        Boolean b = (Boolean) modelRoot.getProperty(ModelRoot.Property.IGNORE_KEY_EVENTS);
        long time = System.currentTimeMillis() - timeArrived;
        boolean show = ((time / 500) % 2) == 0;
        if (show && !b) {
            g.drawImage(cursor, x, y, null);
        }

        // Second, draw a message below the cursor if appropriate.
        String message = (String) modelRoot.getProperty(ModelRoot.Property.CURSOR_MESSAGE);

        if (null != message && !message.isEmpty()) {
            int fontSize = 12;
            Font font = new Font("Arial", 0, fontSize);
            FontRenderContext frc = g2.getFontRenderContext();
            TextLayout layout = new TextLayout(message, font, frc);

            // We want the message to be centered below the cursor.
            float visibleAdvance = layout.getVisibleAdvance();
            float textX = (x + (tileSize.width / 2) - (visibleAdvance / 2));
            float textY = y + tileSize.height + fontSize + 5;
            g.setColor(java.awt.Color.white);
            layout.draw(g2, textX, textY);
        }

        // Draw a big white dot at the target point.
        Vector2D targetPoint = (Vector2D) modelRoot.getProperty(ModelRoot.Property.THINKING_POINT);
        if (null != targetPoint) {
            time = System.currentTimeMillis();
            int dotSize;

            if ((time % 500) > 250) {
                dotSize = ClientConfig.BIG_DOT_WIDTH;
            } else {
                dotSize = ClientConfig.SMALL_DOT_WIDTH;
            }

            g.setColor(Color.WHITE);

            x = targetPoint.x * tileSize.width + (tileSize.width - dotSize) / 2;
            y = targetPoint.y * tileSize.width + (tileSize.height - dotSize) / 2;
            g.fillOval(x, y, dotSize, dotSize);
        }
    }

}