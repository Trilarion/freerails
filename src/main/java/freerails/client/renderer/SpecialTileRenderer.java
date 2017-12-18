/*
 * SpecialTileView.java
 *
 * Created on 20 August 2001, 15:41
 */
package freerails.client.renderer;

import freerails.client.common.ImageManager;
import freerails.world.terrain.TerrainType;
import freerails.world.top.ReadOnlyWorld;
import org.apache.log4j.Logger;

import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * A special tile's icon gets drawn over the icon of a normal tile.
 *
 * @author Luke Lindsay
 */
final public class SpecialTileRenderer extends AbstractTileRenderer {
    private static final Logger logger = Logger
            .getLogger(SpecialTileRenderer.class.getName());

    final private TileRenderer parentTileView;

    /**
     *
     * @param imageManager
     * @param rgbValues
     * @param tileModel
     * @param parentTileView
     * @param w
     * @throws IOException
     */
    public SpecialTileRenderer(ImageManager imageManager, int[] rgbValues,
                               TerrainType tileModel, TileRenderer parentTileView, ReadOnlyWorld w)
            throws IOException {
        super(tileModel, rgbValues, w);
        this.setTileIcons(new Image[1]);
        this.getTileIcons()[0] = imageManager.getImage(generateFilename());
        this.parentTileView = parentTileView;
    }

    /**
     *
     * @param g
     * @param renderX
     * @param renderY
     * @param mapX
     * @param mapY
     * @param w
     */
    @Override
    public void renderTile(java.awt.Graphics g, int renderX, int renderY,
                           int mapX, int mapY, ReadOnlyWorld w) {
        if (parentTileView != null) {
            parentTileView.renderTile(g, renderX, renderY, mapX, mapY, w);
        } else {
            logger.warn("parent tileView==null");
        }

        Image icon = this.getIcon(mapX, mapX, w);

        if (null != icon) {
            g.drawImage(icon, renderX, renderY, null);
        } else {
            logger.warn("special tileView icon==null");
        }
    }

    /**
     *
     * @param x
     * @param y
     * @param w
     * @return
     */
    @Override
    public int selectTileIcon(int x, int y, ReadOnlyWorld w) {
        return 0;
    }

    @Override
    public void dumpImages(ImageManager imageManager) {
        imageManager.setImage(generateFilename(), this.getTileIcons()[0]);
    }

    private String generateFilename() {
        return "terrain" + File.separator + this.getTerrainType() + ".png";
    }

    /**
     *
     * @param i
     * @return
     */
    @Override
    protected String generateFileNameNumber(int i) {
        throw new UnsupportedOperationException();
    }
}