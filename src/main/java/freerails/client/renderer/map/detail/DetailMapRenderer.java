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

package freerails.client.renderer.map.detail;

import freerails.client.ClientConstants;
import freerails.client.renderer.map.*;
import freerails.client.renderer.track.BuildTrackRenderer;
import freerails.util.ui.Painter;
import freerails.client.renderer.*;
import freerails.client.ModelRoot;
import freerails.util.Vec2D;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.ModelConstants;

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
     * @param world
     * @param rendererRoot
     * @param modelRoot
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
     * @return
     */
    public StationRadiusRenderer getStationRadius() {
        return stationRadius;
    }

    /**
     * @return
     */
    @Override
    public float getScale() {
        return ModelConstants.TILE_SIZE;
    }

    /**
     * @return
     */
    @Override
    public Vec2D getMapSizeInPixels() {
        return mapSizeInPixels;
    }

    /**
     * @param g
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
     */
    @Override
    public void refreshTile(Vec2D tileLocation) {
        background.refreshTile(tileLocation);
    }

    /**
     * @param g
     * @param visibleRect
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
     *
     */
    @Override
    public void refreshAll() {
        background.refreshAll();
    }
}
