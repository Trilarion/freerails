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
 *  BackgroundMapView.java
 *
 *  Created on 06 August 2001, 17:21
 */
package freerails.client.renderer.map;

import freerails.client.renderer.track.TrackLayerRenderer;
import freerails.client.renderer.*;
import freerails.util.IntPoint2D;
import freerails.model.world.UnmodifiableWorld;
import freerails.client.ClientConstants;

import java.awt.*;

/**
 * Encapsulates the objects that make-up and paint the background of
 * the map view. At present it is composed of two layers: the terrain layer and
 * the track layer.
 */
public class MapBackgroundRenderer implements MapLayerRenderer {

    /**
     * The terrain layer.
     */
    private final TerrainLayerRenderer terrainLayer;

    /**
     * The track layer.
     */
    private final TrackLayerRenderer trackLayer;
    /*
     * Used to avoid having to create a new rectangle for each call to the paint
     * methods.
     */
    private Rectangle clipRectangle = new Rectangle();

    /**
     * Creates a map background renderer instance.
     *
     * @param world the game world being queried
     * @param rendererRoot the renderer root parameter
     */
    public MapBackgroundRenderer(UnmodifiableWorld world, RendererRoot rendererRoot) {
        trackLayer = new TrackLayerRenderer(world, rendererRoot);
        terrainLayer = new TerrainLayerRenderer(world, rendererRoot);
    }

    /**
     * Performs the paint tile operation.
     *
     * @param g the graphics context used for drawing
     * @param tileLocation the map coordinate
     */
    @Override
    public void paintTile(Graphics g, IntPoint2D tileLocation) {
        terrainLayer.paintTile(g, tileLocation);
        trackLayer.paintTile(g, tileLocation);
    }

    /**
     * A portion of the map has changed, redraw the map.
     *
     * @param g the graphics context used for drawing
     * @param visibleRect the visible area to render
     */
    @Override
    public void paintRect(Graphics g, Rectangle visibleRect) {
        int tileWidth = ClientConstants.TILE_PIXEL_SIZE;
        int tileHeight = ClientConstants.TILE_PIXEL_SIZE;

        clipRectangle = g.getClipBounds(clipRectangle);

        int x = clipRectangle.x / tileWidth;
        int y = clipRectangle.y / tileHeight;
        int width = clipRectangle.width / tileWidth + 2;
        int height = clipRectangle.height / tileHeight + 2;

        terrainLayer.paintRectangleOfTiles(g, x, y, width, height);
        trackLayer.paintRectangleOfTiles(g, new IntPoint2D(x, y), width, height);
    }

    /**
     * Performs the refresh tile operation.
     *
     * @param tileLocation the map coordinate
     */
    @Override
    public void refreshTile(IntPoint2D tileLocation) {}

    /**
     * Performs the refresh all operation.
     */
    @Override
    public void refreshAll() {}

}