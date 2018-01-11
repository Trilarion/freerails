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

package freerails.client.view;

import freerails.client.ClientConfig;
import freerails.client.common.Painter;
import freerails.client.renderer.*;
import freerails.controller.ModelRoot;
import freerails.world.ReadOnlyWorld;

import java.awt.*;

/**
 * Draws the main map, that is the terrain, track, trains, station names etc.
 */
public class DetailMapRenderer implements MapRenderer {
    private static final boolean OSXWorkaround = (System.getProperty("OSXWorkaround") != null);

    private final MapLayerRenderer background;

    private final Dimension mapSizeInPixels;

    private final OverHeadTrainView trainsview;

    private final StationRadiusRenderer stationRadius;

    private final BuildTrackRenderer buildTrackRenderer;

    private final BuildTrackController buildTrackController;

    private final Painter stationBoxes;

    /**
     * @param world
     * @param rr
     * @param modelRoot
     */
    public DetailMapRenderer(ReadOnlyWorld world, RendererRoot rr, ModelRoot modelRoot) {
        trainsview = new OverHeadTrainView(world, rr, modelRoot);

        MapBackgroundRender render = new MapBackgroundRender(world, rr, modelRoot);

        if (OSXWorkaround) {
            // Don't buffer the mapviews background.
            background = render;
        } else {
            background = new SquareTileBackgroundRenderer(render);
        }

        Dimension mapSize = new Dimension(world.getMapWidth(), world.getMapHeight());
        mapSizeInPixels = new Dimension(mapSize.width * ClientConfig.TILE_SIZE, mapSize.height * ClientConfig.TILE_SIZE);
        stationRadius = new StationRadiusRenderer(modelRoot);
        buildTrackRenderer = new BuildTrackRenderer(rr, modelRoot);
        buildTrackController = new BuildTrackController(world, modelRoot);
        stationBoxes = new StationBoxRenderer(world, rr, modelRoot);
    }

    /**
     * @return
     */
    public StationRadiusRenderer getStationRadius() {
        return stationRadius;
    }

    /**
     * @return
     */
    public BuildTrackController getBuildTrackController() {
        return buildTrackController;
    }

    /**
     * @return
     */
    public float getScale() {
        return ClientConfig.TILE_SIZE;
    }

    /**
     * @return
     */
    public Dimension getMapSizeInPixels() {
        return mapSizeInPixels;
    }

    /**
     * @param g
     * @param tileX
     * @param tileY
     */
    public void paintTile(Graphics g, int tileX, int tileY) {
        background.paintTile(g, tileX, tileY);
        trainsview.paint((Graphics2D) g, null);
        stationRadius.paint((Graphics2D) g, null);
        stationBoxes.paint((Graphics2D) g, null);

        buildTrackRenderer.paint((Graphics2D) g, null);
    }

    /**
     * @param x
     * @param y
     */
    public void refreshTile(int x, int y) {
        background.refreshTile(x, y);
    }

    /**
     * @param g
     * @param visibleRect
     */
    public void paintRect(Graphics g, Rectangle visibleRect) {
        background.paintRect(g, visibleRect);
        trainsview.paint((Graphics2D) g, visibleRect);
        stationRadius.paint((Graphics2D) g, null);
        stationBoxes.paint((Graphics2D) g, visibleRect);
        buildTrackRenderer.paint((Graphics2D) g, null);
    }

    /**
     *
     */
    public void refreshAll() {
        background.refreshAll();
    }
}
