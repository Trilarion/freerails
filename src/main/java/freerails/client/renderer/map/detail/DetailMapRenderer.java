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

package freerails.client.renderer.map.detail;

import freerails.client.ClientConstants;
import freerails.client.renderer.map.*;
import freerails.client.renderer.track.BuildTrackRenderer;
import freerails.util.ui.Painter;
import freerails.client.renderer.*;
import freerails.client.ModelRoot;
import freerails.util.Vec2D;
import freerails.model.world.UnmodifiableWorld;

import java.awt.*;

/**
 * Draws the main map, that is the terrain, track, trains, station names etc.
 */
public class DetailMapRenderer implements MapRenderer {

    private final MapLayerRenderer background;
    private final Vec2D mapSizeInPixels;
    private final OverHeadTrainView trainsview;
    private final StationRadiusRenderer stationRadius;
    private final BuildTrackRenderer buildTrackRenderer;
    private final Painter stationBoxes;

    /**
     * Creates a detail map renderer instance.
     *
     * @param world the game world being queried
     * @param rendererRoot the renderer root parameter
     * @param modelRoot the client model that provides current game state
     */
    public DetailMapRenderer(UnmodifiableWorld world, RendererRoot rendererRoot, ModelRoot modelRoot) {
        trainsview = new OverHeadTrainView(world, rendererRoot, modelRoot);

        MapBackgroundRenderer mapBackgroundRenderer = new MapBackgroundRenderer(world, rendererRoot, modelRoot);
        background = new SquareTileBackgroundRenderer(mapBackgroundRenderer);

        Vec2D mapSize = world.getMapSize();
        mapSizeInPixels = Vec2D.multiply(mapSize, ClientConstants.TILE_SIZE);

        stationRadius = new StationRadiusRenderer(modelRoot);
        buildTrackRenderer = new BuildTrackRenderer(rendererRoot, modelRoot);
        stationBoxes = new StationBoxRenderer(world, rendererRoot, modelRoot);
    }

    /**
     * Returns the station radius.
     *
     * @return the station radius produced by this method
     */
    public StationRadiusRenderer getStationRadius() {
        return stationRadius;
    }

    /**
     * Performs the scale operation.
     *
     * @return the scale produced by this method
     */
    @Override
    public float scale() {
        return ClientConstants.TILE_PIXEL_SIZE;
    }

    /**
     * Returns the map size in pixels.
     *
     * @return the map size in pixels produced by this method
     */
    @Override
    public Vec2D getMapSizeInPixels() {
        return mapSizeInPixels;
    }

    /**
     * Performs the paint tile operation.
     *
     * @param g the graphics context used for drawing
     */
    @Override
    public void paintTile(Graphics g, Vec2D tileLocation) {
        background.paintTile(g, tileLocation);
        trainsview.paint((Graphics2D) g, null);
        stationRadius.paint((Graphics2D) g, null);
        stationBoxes.paint((Graphics2D) g, null);

        buildTrackRenderer.paint((Graphics2D) g, null);
    }

    /**
     * Refreshes the background tile at the specified map location.
     *
     * @param tileLocation location of the tile to refresh
     */
    @Override
    public void refreshTile(Vec2D tileLocation) {
        background.refreshTile(tileLocation);
    }

    /**
     * Performs the paint rect operation.
     *
     * @param g the graphics context used for drawing
     * @param visibleRect the visible area to render
     */
    @Override
    public void paintRect(Graphics g, Rectangle visibleRect) {
        background.paintRect(g, visibleRect);
        trainsview.paint((Graphics2D) g, visibleRect);
        stationRadius.paint((Graphics2D) g, null);
        stationBoxes.paint((Graphics2D) g, visibleRect);
        buildTrackRenderer.paint((Graphics2D) g, null);
    }

    /**
     * Performs the refresh all operation.
     */
    @Override
    public void refreshAll() {
        background.refreshAll();
    }
}
