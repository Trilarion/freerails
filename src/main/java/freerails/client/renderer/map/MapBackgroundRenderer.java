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
 *  BackgroundMapView.java
 *
 *  Created on 06 August 2001, 17:21
 */
package freerails.client.renderer.map;

import freerails.util.ui.Painter;
import freerails.client.renderer.*;
import freerails.client.ModelRoot;
import freerails.util.Vector2D;
import freerails.model.world.ReadOnlyWorld;
import freerails.model.WorldConstants;
import org.apache.log4j.Logger;

import java.awt.*;

/**
 * Encapsulates the objects that make-up and paint the background of
 * the map view. At present it is composed of two layers: the terrain layer and
 * the track layer.
 */
public class MapBackgroundRenderer implements MapLayerRenderer {

    private static final Logger logger = Logger.getLogger(MapBackgroundRenderer.class.getName());

    /**
     * The terrain layer.
     */
    private final TerrainLayerRenderer terrainLayer;

    /**
     * The track layer.
     */
    private final TrackLayerRenderer trackLayer;
    private final Vector2D mapSize;
    private final Painter cityNames;
    private final Painter stationNames;

    /*
     * Used to avoid having to create a new rectangle for each call to the paint
     * methods.
     */
    private Rectangle clipRectangle = new Rectangle();

    /**
     * @param world
     * @param rendererRoot
     * @param modelRoot
     */
    public MapBackgroundRenderer(ReadOnlyWorld world, RendererRoot rendererRoot, ModelRoot modelRoot) {
        trackLayer = new TrackLayerRenderer(world, rendererRoot);
        terrainLayer = new TerrainLayerRenderer(world, rendererRoot);
        mapSize = world.getMapSize();
        cityNames = new CityNamesRenderer(world);
        stationNames = new StationNamesRenderer(world, modelRoot);
    }

    /**
     * @param g
     * @param tileLocation
     */
    public void paintTile(Graphics g, Vector2D tileLocation) {
        terrainLayer.paintTile(g, tileLocation);
        trackLayer.paintTile(g, tileLocation);
        cityNames.paint((Graphics2D) g, null);
        stationNames.paint((Graphics2D) g, null);
    }

    /**
     * @param g
     * @param visibleRect
     */
    public void paintRect(Graphics g, Rectangle visibleRect) {
        int tileWidth = WorldConstants.TILE_SIZE;
        int tileHeight = WorldConstants.TILE_SIZE;

        clipRectangle = g.getClipBounds(clipRectangle);

        int x = clipRectangle.x / tileWidth;
        int y = clipRectangle.y / tileHeight;
        int width = (clipRectangle.width / tileWidth) + 2;
        int height = (clipRectangle.height) / tileHeight + 2;

        paintRectangleOfTiles(g, x, y, width, height);
        cityNames.paint((Graphics2D) g, visibleRect);
        stationNames.paint((Graphics2D) g, visibleRect);
    }

    private void paintRectangleOfTiles(Graphics g, int x, int y, int width, int height) {
        terrainLayer.paintRectangleOfTiles(g, x, y, width, height);
        trackLayer.paintRectangleOfTiles(g, new Vector2D(x, y), width, height);
        Rectangle visibleRectangle = new Rectangle(x * WorldConstants.TILE_SIZE, y * WorldConstants.TILE_SIZE, width * WorldConstants.TILE_SIZE, height * WorldConstants.TILE_SIZE);
        cityNames.paint((Graphics2D) g, visibleRectangle);
        stationNames.paint((Graphics2D) g, visibleRectangle);
    }

    /**
     * @param tileLocation
     */
    public void refreshTile(Vector2D tileLocation) {}

    /**
     *
     */
    public void refreshAll() {}

}