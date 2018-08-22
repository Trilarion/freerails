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
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package freerails.client.renderer.map.overview;

import freerails.client.ARGBColor;
import freerails.client.renderer.map.MapRenderer;
import freerails.io.GsonManager;
import freerails.util.Vec2D;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.terrain.TerrainTile;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

/**
 * Draws the overview map.
 */
public class OverviewMapRenderer implements MapRenderer {

    private final Vec2D imageSize;
    private final Vec2D mapSize;
    private final Vec2D mapLocation = Vec2D.ZERO;
    private final UnmodifiableWorld world;
    private final AffineTransform affineTransform;
    // TODO used many time, could be static
    private final GraphicsConfiguration defaultConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    private BufferedImage oneToOneImage;
    private BufferedImage mapImage;
    private boolean isDirty = true;
    private Map<Integer, ARGBColor> terrainColors;

    private OverviewMapRenderer(UnmodifiableWorld world, Vec2D imageSize) {
        this.world = world;
        mapSize = world.getMapSize();
        this.imageSize = imageSize;

        // TODO this should probably be loaded by the client at some point before (should they be model or client specific)
        URL url = OverviewMapRenderer.class.getResource("/freerails/client/terrain_colors.json");
        File file = null;
        try {
            file = new File(url.toURI());
            terrainColors = GsonManager.loadTerrainColors(file);
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }

        double scalingFactor = ((double) imageSize.y) / mapSize.y;
        affineTransform = AffineTransform.getScaleInstance(scalingFactor, scalingFactor);
        refresh();
    }

    /**
     * @param world
     * @param maxSize
     * @return
     */
    public static MapRenderer getInstance(UnmodifiableWorld world, Dimension maxSize) {
        // Work with doubles to avoid rounding errors.
        Vec2D mapSize = world.getMapSize();
        double worldWidth = mapSize.x;
        double worldHeight = mapSize.y;
        double scale;

        if (worldWidth / worldHeight > maxSize.getWidth() / maxSize.getHeight()) {
            scale = maxSize.getWidth() / worldWidth;
        } else {
            scale = maxSize.getHeight() / worldHeight;
        }

        double height = scale * worldHeight;
        double width = scale * worldWidth;

        return new OverviewMapRenderer(world, new Vec2D((int) width, (int) height));
    }

    /**
     * @return
     */
    @Override
    public float getScale() {
        return (float) imageSize.y / (float) mapSize.y;
    }

    /**
     * @param g
     * @param visibleRect
     */
    @Override
    public void paintRect(Graphics g, Rectangle visibleRect) {
        renderOffScreenImage();

        g.drawImage(mapImage, 0, 0, null);
    }

    private void renderOffScreenImage() {
        if (isDirty) {
            Graphics2D mapGraphics = mapImage.createGraphics();

            mapGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            mapGraphics.setClip(0, 0, imageSize.x, imageSize.y);
            mapGraphics.clearRect(0, 0, imageSize.x, imageSize.y);
            mapGraphics.drawImage(oneToOneImage, affineTransform, null);
            isDirty = false;
        }
    }

    @Override
    public void refreshTile(Vec2D tileLocation) {
        TerrainTile terrainTile = (TerrainTile) world.getTile(tileLocation);

        if (terrainTile.getTrackPiece() == null) {
            int terrainTypeId = terrainTile.getTerrainTypeId();
            oneToOneImage.setRGB(tileLocation.x, tileLocation.y, terrainColors.get(terrainTypeId).getARGB());
        } else {
            // black with alpha of 1
            oneToOneImage.setRGB(tileLocation.x, tileLocation.y, 0xff000000);
        }

        isDirty = true;
    }

    /**
     * Redraw the whole map onto a new buffer.
     */
    private void refresh() {
        isDirty = true;

        // free up memory used by the old image
        if (mapImage != null) {
            mapImage.flush();
        }

        if (oneToOneImage != null) {
            oneToOneImage.flush();
        }

        // generate a 1:1 map of the terrain layer
        oneToOneImage = defaultConfiguration.createCompatibleImage(mapSize.x, mapSize.y, Transparency.TRANSLUCENT);
        mapImage = defaultConfiguration.createCompatibleImage(imageSize.x, imageSize.y, Transparency.OPAQUE);


        for (int tileX = mapLocation.x; tileX < mapSize.x + mapLocation.x; tileX++) {
            for (int tileY = mapLocation.y; tileY < mapSize.y + mapLocation.y; tileY++) {
                TerrainTile terrainTile = (TerrainTile) world.getTile(new Vec2D(tileX, tileY));

                if (terrainTile.getTrackPiece() == null) {
                    int terrainTypeId = terrainTile.getTerrainTypeId();
                    oneToOneImage.setRGB(tileX - mapLocation.x, tileY - mapLocation.y, terrainColors.get(terrainTypeId).getARGB());
                } else {
                    // black with alpha of 1
                    oneToOneImage.setRGB(tileX - mapLocation.x, tileY - mapLocation.y, 0xff000000);
                }
            }
        }

        renderOffScreenImage();
    }

    /**
     * @return
     */
    @Override
    public Vec2D getMapSizeInPixels() {
        return imageSize;
    }

    /**
     * @param g
     * @param tileLocation
     */
    @Override
    public void paintTile(Graphics g, Vec2D tileLocation) {
        g.drawImage(mapImage, 0, 0, null);
    }

    /**
     *
     */
    @Override
    public void refreshAll() {
        refresh();
    }
}