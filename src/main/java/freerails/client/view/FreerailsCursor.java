/*
  Created on 01 August 2001, 06:02
 */
package freerails.client.view;

import freerails.client.renderer.BuildTrackRenderer;
import freerails.client.renderer.RenderersRoot;
import freerails.controller.ModelRoot;
import freerails.controller.TrackMoveProducer;
import freerails.world.common.ImPoint;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.io.IOException;

/**
 * Paints the cursor on the map, note the cursor's position is stored on the
 * ModelRoot under the key CURSOR_POSITION.
 *
 * @author Luke
 */
final public class FreerailsCursor {
    private final Image buildTrack, upgradeTrack, removeTrack, infoMode;

    private final ModelRoot modelRoot;

    /** The location of the cursor last time paintCursor(.) was called. */
    private ImPoint lastCursorPosition = new ImPoint();

    /** The time in ms the cursor arrived at its current position. */
    private long timeArrived = 0;

    /**
     * Creates a new FreerailsCursor.
     *
     * @throws IOException
     */
    public FreerailsCursor(ModelRoot mr, RenderersRoot rr) throws IOException {
        this.modelRoot = mr;
        modelRoot.setProperty(ModelRoot.Property.CURSOR_MESSAGE, null);
        buildTrack = rr.getImage("cursor/buildtrack.png");
        upgradeTrack = rr.getImage("cursor/upgradetrack.png");
        removeTrack = rr.getImage("cursor/removetrack.png");
        infoMode = rr.getImage("cursor/infomode.png");
    }

    /**
     * Paints the cursor. The method calculates position to paint it based on
     * the tile size and the cursor's map position.
     *
     * @param g
     *            The graphics object to paint the cursor on.
     * @param tileSize
     *            The dimensions of a tile.
     */
    public void paintCursor(Graphics g, Dimension tileSize) {
        Graphics2D g2 = (Graphics2D) g;

        TrackMoveProducer.BuildMode buildMode = (TrackMoveProducer.BuildMode) modelRoot
                .getProperty(ModelRoot.Property.TRACK_BUILDER_MODE);

        ImPoint cursorMapPosition = (ImPoint) modelRoot
                .getProperty(ModelRoot.Property.CURSOR_POSITION);

        /* Has the cursor moved since we last painted it? */
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

        Boolean b = (Boolean) modelRoot
                .getProperty(ModelRoot.Property.IGNORE_KEY_EVENTS);
        long time = System.currentTimeMillis() - timeArrived;
        boolean show = ((time / 500) % 2) == 0;
        if (show && !b) {
            g.drawImage(cursor, x, y, null);
        }

        // Second, draw a message below the cursor if appropriate.
        String message = (String) modelRoot
                .getProperty(ModelRoot.Property.CURSOR_MESSAGE);

        if (null != message && !message.equals("")) {
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
        ImPoint targetPoint = (ImPoint) modelRoot
                .getProperty(ModelRoot.Property.THINKING_POINT);
        if (null != targetPoint) {
            time = System.currentTimeMillis();
            int dotSize;

            if ((time % 500) > 250) {
                dotSize = BuildTrackRenderer.BIG_DOT_WIDTH;
            } else {
                dotSize = BuildTrackRenderer.SMALL_DOT_WIDTH;
            }

            g.setColor(Color.WHITE);

            x = targetPoint.x * tileSize.width + (tileSize.width - dotSize) / 2;
            y = targetPoint.y * tileSize.width + (tileSize.height - dotSize)
                    / 2;
            g.fillOval(x, y, dotSize, dotSize);
        }
    }

}