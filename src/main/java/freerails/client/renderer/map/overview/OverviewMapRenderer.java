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

import freerails.client.renderer.map.MapRenderer;
import freerails.io.ARGBColor;
import freerails.util.IntPoint2D;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.terrain.TerrainTile;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Map;

/**
 * Draws the overview map.
 */
public class OverviewMapRenderer implements MapRenderer {

    private final IntPoint2D imageSize;
    private final IntPoint2D mapSize;
    private final IntPoint2D mapLocation = IntPoint2D.ZERO;
    private final UnmodifiableWorld world;
    private final AffineTransform affineTransform;
    private BufferedImage oneToOneImage;
    private BufferedImage mapImage;
    private boolean isDirty = true;
    private final Map<Integer, ARGBColor> terrainColors;

    /**
     * Creates a overview map renderer instance.
     * @param world the world
     * @param imageSize the image size
     * @param terrainColors the overview color of every terrain type, keyed by terrain ID
     */
    private OverviewMapRenderer(UnmodifiableWorld world, IntPoint2D imageSize, Map<Integer, ARGBColor> terrainColors) {
        this.world = world;
        mapSize = world.getMapSize();
        this.imageSize = imageSize;
        this.terrainColors = terrainColors;

        double scalingFactor = (double) imageSize.y() / mapSize.y();
        affineTransform = AffineTransform.getScaleInstance(scalingFactor, scalingFactor);
        refresh();
    }

    /**
     * Returns the instance.
     *
     * @param world the game world being queried
     * @param maxSize the max size parameter
     * @param terrainColors the overview color of every terrain type, keyed by terrain ID
     * @return the instance produced by this method
     */
    public static MapRenderer getInstance(UnmodifiableWorld world, Dimension maxSize, Map<Integer, ARGBColor> terrainColors) {
        // Work with doubles to avoid rounding errors.
        IntPoint2D mapSize = world.getMapSize();
        double worldWidth = mapSize.x();
        double worldHeight = mapSize.y();
        double scale;

        if (worldWidth / worldHeight > maxSize.getWidth() / maxSize.getHeight()) {
            scale = maxSize.getWidth() / worldWidth;
        } else {
            scale = maxSize.getHeight() / worldHeight;
        }

        double height = scale * worldHeight;
        double width = scale * worldWidth;

        return new OverviewMapRenderer(world, new IntPoint2D((int) width, (int) height), terrainColors);
    }

    /**
     * Performs the scale operation.
     *
     * @return the scale produced by this method
     */
    @Override
    public float scale() {
        return (float) imageSize.y() / (float) mapSize.y();
    }

    /**
     * Paints the whole overview image; the overview has no dynamic overlays.
     *
     * @param g the graphics context used for drawing
     * @param visibleRect the visible area to render, ignored
     */
    @Override
    public void paintBackgroundRect(Graphics g, Rectangle visibleRect) {
        renderOffScreenImage();

        g.drawImage(mapImage, 0, 0, null);
    }

    /**
     * Renders the off screen image.
     */
    private void renderOffScreenImage() {
        if (isDirty) {
            Graphics2D mapGraphics = mapImage.createGraphics();

            mapGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            mapGraphics.setClip(0, 0, imageSize.x(), imageSize.y());
            mapGraphics.clearRect(0, 0, imageSize.x(), imageSize.y());
            mapGraphics.drawImage(oneToOneImage, affineTransform, null);
            isDirty = false;
        }
    }

    /**
     * Refresh tile.
     * @param tileLocation the tile location
     */
    @Override
    public void refreshTile(IntPoint2D tileLocation) {
        TerrainTile terrainTile = world.getTile(tileLocation);

        if (terrainTile.trackPiece() == null) {
            int terrainTypeId = terrainTile.terrainTypeId();
            oneToOneImage.setRGB(tileLocation.x(), tileLocation.y(), terrainColors.get(terrainTypeId).getARGB());
        } else {
            // black with alpha of 1
            oneToOneImage.setRGB(tileLocation.x(), tileLocation.y(), 0xff000000);
        }

        isDirty = true;
    }

    /**
     * Redraw the whole map onto a new buffer.
     */
    private void refresh() {
        isDirty = true;

        // Plain in-memory images: the map view copies the overview into an
        // ARGB image for JavaFX anyway, so screen-compatible images gain nothing.
        // A 1:1 map of the terrain layer, one pixel per tile.
        oneToOneImage = new BufferedImage(mapSize.x(), mapSize.y(), BufferedImage.TYPE_INT_ARGB);
        mapImage = new BufferedImage(imageSize.x(), imageSize.y(), BufferedImage.TYPE_INT_RGB);

        for (int tileX = mapLocation.x(); tileX < mapSize.x() + mapLocation.x(); tileX++) {
            for (int tileY = mapLocation.y(); tileY < mapSize.y() + mapLocation.y(); tileY++) {
                TerrainTile terrainTile = world.getTile(new IntPoint2D(tileX, tileY));

                if (terrainTile.trackPiece() == null) {
                    int terrainTypeId = terrainTile.terrainTypeId();
                    oneToOneImage.setRGB(tileX - mapLocation.x(), tileY - mapLocation.y(), terrainColors.get(terrainTypeId).getARGB());
                } else {
                    // black with alpha of 1
                    oneToOneImage.setRGB(tileX - mapLocation.x(), tileY - mapLocation.y(), 0xff000000);
                }
            }
        }

        renderOffScreenImage();
    }

    /**
     * Returns the map size in pixels.
     *
     * @return the map size in pixels produced by this method
     */
    @Override
    public IntPoint2D getMapSizeInPixels() {
        return imageSize;
    }

    /**
     * Performs the refresh all operation.
     */
    @Override
    public void refreshAll() {
        refresh();
    }
}