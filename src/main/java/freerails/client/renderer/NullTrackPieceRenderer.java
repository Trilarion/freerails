/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package freerails.client.renderer;

import freerails.client.common.ImageManager;

import java.awt.*;

/**
 * This class implements the TrackPieceView interface, but intentionally does
 * nothing. Its methods are called when drawing tiles with no track.
 *
 */
final public class NullTrackPieceRenderer implements TrackPieceRenderer {

    /**
     *
     */
    public static final NullTrackPieceRenderer instance = new NullTrackPieceRenderer();

    private NullTrackPieceRenderer() {
    }

    /*
     * @see TrackPieceView#getTrackPieceIcon(int)
     */

    /**
     *
     * @param trackTemplate
     * @return
     */

    public Image getTrackPieceIcon(int trackTemplate) {
        return null;
    }

    /*
     * @see TrackPieceView#drawTrackPieceIcon(int, Graphics, int, int,
     *      Dimension)
     */

    /**
     *
     * @param trackTemplate
     * @param g
     * @param x
     * @param y
     * @param tileSize
     */

    public void drawTrackPieceIcon(int trackTemplate, Graphics g, int x, int y,
                                   Dimension tileSize) {
        // Draw nothing since there no track here.
    }

    public void dumpImages(ImageManager imageManager) {
        // TODO Auto-generated method stub
    }
}