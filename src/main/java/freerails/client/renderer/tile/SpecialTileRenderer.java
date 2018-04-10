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

import freerails.util.Vec2D;
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
     * @throws IOException
     */
    public SpecialTileRenderer(ImageManager imageManager, int[] rgbValues, TerrainType tileModel, TileRenderer parentTileView) throws IOException {
        super(tileModel, rgbValues, 1);
        getTileIcons()[0] = imageManager.getImage(generateFilename());
        this.parentTileView = parentTileView;
    }

    /**
     * @param g
     * @param renderLocation
     * @param mapLocation
     * @param world
     */
    @Override
    public void render(Graphics g, Vec2D renderLocation, Vec2D mapLocation, ReadOnlyWorld world) {
        if (parentTileView != null) {
            parentTileView.render(g, renderLocation, mapLocation, world);
        } else {
            logger.warn("parent tileView==null");
        }

        Image icon = getIcon(mapLocation, world);

        if (null != icon) {
            g.drawImage(icon, renderLocation.x, renderLocation.y, null);
        } else {
            logger.warn("special tileView icon==null");
        }
    }

    /**
     * @param mapLocation
     * @param world
     * @return
     */
    @Override
    public int selectTileIconIndex(Vec2D mapLocation, ReadOnlyWorld world) {
        return 0;
    }

    // TODO move this somewhere else
    private String generateFilename() {
        return "terrain" + File.separator + getTerrainTypeName() + ".png";
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