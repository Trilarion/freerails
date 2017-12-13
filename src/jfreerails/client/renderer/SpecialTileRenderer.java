/*
* SpecialTileView.java
*
* Created on 20 August 2001, 15:41
*/
package jfreerails.client.renderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import jfreerails.client.common.ImageManager;
import jfreerails.world.terrain.TerrainType;
import jfreerails.world.top.ReadOnlyWorld;


/**
*
* @author  Luke Lindsay
*/
final public class SpecialTileRenderer extends AbstractTileRenderer {
    final private TileRenderer parentTileView;

    public void renderTile(java.awt.Graphics g, int renderX, int renderY,
        int mapX, int mapY, ReadOnlyWorld w) {
        if (parentTileView != null) {
            parentTileView.renderTile(g, renderX, renderY, mapX, mapY, w);
        } else {
            System.err.println("parent tileView==null");
        }

        BufferedImage icon = this.getIcon(mapX, mapX, w);

        if (null != icon) {
            g.drawImage(icon, renderX, renderY, null);
        } else {
            System.err.println("special tileView icon==null");
        }
    }

    public int selectTileIcon(int x, int y, ReadOnlyWorld w) {
        return 0;
    }

    public SpecialTileRenderer(ImageManager imageManager, int[] rgbValues,
        TerrainType tileModel, TileRenderer parentTileView)
        throws IOException {
        super(tileModel, rgbValues);
        this.setTileIcons(new BufferedImage[1]);
        this.getTileIcons()[0] = imageManager.getImage(generateFilename());
        this.parentTileView = parentTileView;
    }

    public void dumpImages(ImageManager imageManager) {
        imageManager.setImage(generateFilename(), this.getTileIcons()[0]);
    }

    private String generateFilename() {
        return "terrain" + File.separator + this.getTerrainType() + ".png";
    }

    protected String generateFileNameNumber(int i) {
        throw new UnsupportedOperationException();
    }
}
