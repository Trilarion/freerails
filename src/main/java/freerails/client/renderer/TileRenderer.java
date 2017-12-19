package freerails.client.renderer;

import freerails.client.common.ImageManager;
import freerails.world.top.ReadOnlyWorld;

import java.awt.*;

/**
 * Draws an icon to represent a tile.
 *
 */
public interface TileRenderer {

    /**
     *
     * @return
     */
    Image getDefaultIcon();

    /**
     *
     * @param g
     * @param renderX
     * @param renderY
     * @param mapX
     * @param mapY
     * @param w
     */
    void renderTile(java.awt.Graphics g, int renderX, int renderY, int mapX,
                    int mapY, ReadOnlyWorld w);

    /**
     * Adds the images this TileRenderer uses to the specified ImageManager.
     * @param imageManager
     */
    void dumpImages(ImageManager imageManager);
}