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
package freerails.client.renderer.map;

import freerails.util.Vector2D;
import freerails.model.world.ReadOnlyWorld;
import freerails.model.SKEY;
import freerails.model.terrain.FullTerrainTile;
import freerails.model.terrain.TerrainType;
import freerails.model.track.NullTrackPiece;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Draws the overview map.
 */
public class ZoomedOutMapRenderer implements MapRenderer {

    private final int imageWidth;
    private final int imageHeight;
    private final int mapWidth;
    private final int mapHeight;
    private final int mapX;
    private final int mapY;
    private final ReadOnlyWorld world;
    private final AffineTransform affineTransform;
    private final GraphicsConfiguration defaultConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
    private BufferedImage oneToOneImage;
    private BufferedImage mapImage;
    private boolean isDirty = true;

    private ZoomedOutMapRenderer(ReadOnlyWorld world, int width, int height, int mapX, int mapY, int mapWidth, int mapHeight) {
        this.world = world;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        imageHeight = height;
        imageWidth = width;

        double scalingFactor = ((double) imageHeight) / mapHeight;
        affineTransform = AffineTransform.getScaleInstance(scalingFactor, scalingFactor);
        this.mapX = mapX;
        this.mapY = mapY;
        refresh();
    }

    /**
     * @param world
     * @param maxSize
     * @return
     */
    public static MapRenderer getInstance(ReadOnlyWorld world, Dimension maxSize) {
        // Work with doubles to avoid rounding errors.
        double worldWidth = world.getMapWidth();
        double worldHeight = world.getMapHeight();
        double scale;

        if (worldWidth / worldHeight > maxSize.getWidth() / maxSize.getHeight()) {
            scale = maxSize.getWidth() / worldWidth;
        } else {
            scale = maxSize.getHeight() / worldHeight;
        }

        double height = scale * worldHeight;
        double width = scale * worldWidth;

        return new ZoomedOutMapRenderer(world, (int) width, (int) height, 0, 0, world.getMapWidth(), world.getMapHeight());
    }

    /**
     * @return
     */
    public float getScale() {
        return (float) imageHeight / (float) mapHeight;
    }

    /**
     * @param g
     * @param visibleRect
     */
    public void paintRect(Graphics g, Rectangle visibleRect) {
        renderOffScreenImage();

        g.drawImage(mapImage, 0, 0, null);
    }

    private void renderOffScreenImage() {
        if (isDirty) {
            Graphics2D mapGraphics = mapImage.createGraphics();

            mapGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            mapGraphics.setClip(0, 0, imageWidth, imageHeight);
            mapGraphics.clearRect(0, 0, imageWidth, imageHeight);
            mapGraphics.drawImage(oneToOneImage, affineTransform, null);
            isDirty = false;
        }
    }

    public void refreshTile(Vector2D tile) {
        FullTerrainTile tt = (FullTerrainTile) world.getTile(tile);

        if (tt.getTrackPiece().equals(NullTrackPiece.getInstance())) {
            int typeNumber = tt.getTerrainTypeID();
            TerrainType terrainType = (TerrainType) world.get(SKEY.TERRAIN_TYPES, typeNumber);
            oneToOneImage.setRGB(tile.x, tile.y, terrainType.getRGB());
        } else {
            // black with alpha of 1
            oneToOneImage.setRGB(tile.x, tile.y, 0xff000000);
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

        // if (mapGraphics != null) {
        // mapGraphics.dispose();
        // }
        // generate a 1:1 map of the terrain layer
        oneToOneImage = defaultConfiguration.createCompatibleImage(mapWidth, mapHeight, Transparency.TRANSLUCENT);
        mapImage = defaultConfiguration.createCompatibleImage(imageWidth, imageHeight, Transparency.OPAQUE);


        for (int tileX = mapX; tileX < mapWidth + mapX; tileX++) {
            for (int tileY = mapY; tileY < mapHeight + mapY; tileY++) {
                FullTerrainTile tt = (FullTerrainTile) world.getTile(new Vector2D(tileX, tileY));

                if (tt.getTrackPiece().equals(NullTrackPiece.getInstance())) {
                    int typeNumber = tt.getTerrainTypeID();
                    TerrainType terrainType = (TerrainType) world.get(SKEY.TERRAIN_TYPES, typeNumber);
                    oneToOneImage.setRGB(tileX - mapX, tileY - mapY, terrainType.getRGB());
                } else {
                    // black with alpha of 1
                    oneToOneImage.setRGB(tileX - mapX, tileY - mapY, 0xff000000);
                }
            }
        }

        renderOffScreenImage();
    }

    /**
     * @return
     */
    public Dimension getMapSizeInPixels() {
        return new Dimension(imageWidth, imageHeight);
    }

    /**
     * @param g
     * @param tileX
     * @param tileY
     */
    public void paintTile(Graphics g, Vector2D tileP) {
        g.drawImage(mapImage, 0, 0, null);
    }

    /**
     *
     */
    public void refreshAll() {
        refresh();
    }
}