package freerails.client.renderer;

import freerails.client.common.ImageManager;

import java.awt.*;

/**
 * Draws an icon to represent a track piece.
 *
 * @author Luke Lindsay 09 October 2001
 */
public interface TrackPieceRenderer {
    Image getTrackPieceIcon(int trackTemplate);

    void drawTrackPieceIcon(int trackTemplate, java.awt.Graphics g, int x,
                            int y, java.awt.Dimension tileSize);

    /**
     * Adds the images this TileRenderer uses to the specified ImageManager.
     */
    void dumpImages(ImageManager imageManager);
}