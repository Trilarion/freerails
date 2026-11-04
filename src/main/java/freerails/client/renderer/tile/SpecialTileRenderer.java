/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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

import freerails.model.terrain.Terrain;
import freerails.util.Vector2D;
import freerails.util.ui.ImageManager;
import freerails.model.world.UnmodifiableWorld;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * A special tile's icon gets drawn over the icon of a normal tile.
 */
public class SpecialTileRenderer extends AbstractTileRenderer {

    private static final TaggedLogger logger = Logger.tag("SpecialTileRenderer");
    private final TileRenderer parentTileView;

    /**
     * Creates a special tile renderer instance.
     *
     * @param imageManager the image manager parameter
     * @param treatTypeAsSame the treat type as same parameter
     * @param tileModel the tile model parameter
     * @param parentTileView the parent tile view parameter
     * @throws IOException if the requested I/O operation cannot be completed.
     */
    public SpecialTileRenderer(ImageManager imageManager, List<Integer> treatTypeAsSame, Terrain tileModel, TileRenderer parentTileView) throws IOException {
        super(tileModel, treatTypeAsSame, 1);
        getTileIcons()[0] = imageManager.getImage("terrain" + File.separator + getTerrainTypeName() + ".png");
        this.parentTileView = parentTileView;
    }

    /**
     * Performs the render operation.
     *
     * @param g the graphics context used for drawing
     * @param renderLocation the map coordinate
     * @param mapLocation the map coordinate
     * @param world the game world being queried
     */
    @Override
    public void render(Graphics g, Vector2D renderLocation, Vector2D mapLocation, UnmodifiableWorld world) {
        if (parentTileView != null) {
            parentTileView.render(g, renderLocation, mapLocation, world);
        } else {
            logger.warn("parent tileView==null");
        }

        Image icon = getIcon(mapLocation, world);

        if (null != icon) {
            g.drawImage(icon, renderLocation.x(), renderLocation.y(), null);
        } else {
            logger.warn("special tileView icon==null");
        }
    }

    /**
     * Selects the tile icon index.
     *
     * @param mapLocation the map coordinate
     * @param world the game world being queried
     * @return the tile icon index produced by this method
     */
    @Override
    public int selectTileIconIndex(Vector2D mapLocation, UnmodifiableWorld world) {
        return 0;
    }

    /**
     * Performs the generate file name number operation.
     *
     * @param i the index of the item
     * @return the file name number produced by this method
     */
    @Override
    protected String generateFileNameNumber(int i) {
        throw new UnsupportedOperationException();
    }
}