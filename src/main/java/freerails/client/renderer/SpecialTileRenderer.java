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
 */
final public class SpecialTileRenderer extends AbstractTileRenderer {
    private static final Logger logger = Logger
            .getLogger(SpecialTileRenderer.class.getName());

    final private TileRenderer parentTileView;

    /**
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
     * @param i
     * @return
     */
    @Override
    protected String generateFileNameNumber(int i) {
        throw new UnsupportedOperationException();
    }
}