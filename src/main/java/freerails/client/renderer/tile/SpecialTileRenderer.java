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
 */
package freerails.client.renderer.tile;

import freerails.util.ui.ImageManager;
import freerails.model.world.ReadOnlyWorld;
import freerails.model.terrain.TerrainType;
import org.apache.log4j.Logger;

import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * A special tile's icon gets drawn over the icon of a normal tile.
 */
public class SpecialTileRenderer extends AbstractTileRenderer {

    private static final Logger logger = Logger.getLogger(SpecialTileRenderer.class.getName());
    private final TileRenderer parentTileView;

    /**
     * @param imageManager
     * @param rgbValues
     * @param tileModel
     * @param parentTileView
     * @param world
     * @throws IOException
     */
    public SpecialTileRenderer(ImageManager imageManager, int[] rgbValues, TerrainType tileModel, TileRenderer parentTileView, ReadOnlyWorld world) throws IOException {
        super(tileModel, rgbValues, world);
        setTileIcons(new Image[1]);
        getTileIcons()[0] = imageManager.getImage(generateFilename());
        this.parentTileView = parentTileView;
    }

    /**
     * @param g
     * @param screenX
     * @param screenY
     * @param mapX
     * @param mapY
     * @param world
     */
    @Override
    public void renderTile(java.awt.Graphics g, int screenX, int screenY, int mapX, int mapY, ReadOnlyWorld world) {
        if (parentTileView != null) {
            parentTileView.renderTile(g, screenX, screenY, mapX, mapY, world);
        } else {
            logger.warn("parent tileView==null");
        }

        Image icon = getIcon(mapX, mapX, world);

        if (null != icon) {
            g.drawImage(icon, screenX, screenY, null);
        } else {
            logger.warn("special tileView icon==null");
        }
    }

    /**
     * @param x
     * @param y
     * @param world
     * @return
     */
    @Override
    public int selectTileIcon(int x, int y, ReadOnlyWorld world) {
        return 0;
    }

    private String generateFilename() {
        return "terrain" + File.separator + getTerrainType() + ".png";
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