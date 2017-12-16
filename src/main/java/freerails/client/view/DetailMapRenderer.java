package freerails.client.view;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import freerails.client.common.Painter;
import freerails.client.renderer.BuildTrackController;
import freerails.client.renderer.BuildTrackRenderer;
import freerails.client.renderer.MapBackgroundRender;
import freerails.client.renderer.MapLayerRenderer;
import freerails.client.renderer.MapRenderer;
import freerails.client.renderer.RenderersRoot;
import freerails.client.renderer.SquareTileBackgroundRenderer;
import freerails.client.renderer.StationBoxRenderer;
import freerails.client.renderer.StationRadiusRenderer;
import freerails.controller.ModelRoot;
import freerails.world.Constants;
import freerails.world.top.ReadOnlyWorld;

/**
 * Draws the main map, that is the terrain, track, trains, station names etc.
 * 
 * @author Luke
 */
public class DetailMapRenderer implements MapRenderer {
    private static final boolean OSXWorkaround = (System
            .getProperty("OSXWorkaround") != null);

    private final MapLayerRenderer background;

    private final Dimension mapSizeInPixels;

    private final OverHeadTrainView trainsview;

    private final StationRadiusRenderer stationRadius;

    private final BuildTrackRenderer buildTrackRenderer;

    private final BuildTrackController buildTrackController;

    private final Painter stationBoxes;

    public DetailMapRenderer(ReadOnlyWorld world, RenderersRoot rr,
            ModelRoot modelRoot) {
        trainsview = new OverHeadTrainView(world, rr, modelRoot);

        MapBackgroundRender render = new MapBackgroundRender(world, rr,
                modelRoot);

        if (OSXWorkaround) {
            // Don't buffer the mapviews background.
            background = render;
        } else {
            background = new SquareTileBackgroundRenderer(render);
        }

        Dimension mapSize = new Dimension(world.getMapWidth(), world
                .getMapHeight());
        mapSizeInPixels = new Dimension(mapSize.width * Constants.TILE_SIZE,
                mapSize.height * Constants.TILE_SIZE);
        stationRadius = new StationRadiusRenderer(modelRoot);
        buildTrackRenderer = new BuildTrackRenderer(rr, modelRoot);
        buildTrackController = new BuildTrackController(world, modelRoot);
        stationBoxes = new StationBoxRenderer(world, rr, modelRoot);
    }

    public StationRadiusRenderer getStationRadius() {
        return stationRadius;
    }

    public BuildTrackController getBuildTrackController() {
        return buildTrackController;
    }

    public float getScale() {
        return Constants.TILE_SIZE;
    }

    public Dimension getMapSizeInPixels() {
        return mapSizeInPixels;
    }

    public void paintTile(Graphics g, int tileX, int tileY) {
        background.paintTile(g, tileX, tileY);
        trainsview.paint((Graphics2D) g, null);
        stationRadius.paint((Graphics2D) g, null);
        stationBoxes.paint((Graphics2D) g, null);

        buildTrackRenderer.paint((Graphics2D) g, null);
    }

    public void refreshTile(int x, int y) {
        background.refreshTile(x, y);
    }

    public void paintRect(Graphics g, Rectangle visibleRect) {
        background.paintRect(g, visibleRect);
        trainsview.paint((Graphics2D) g, visibleRect);
        stationRadius.paint((Graphics2D) g, null);
        stationBoxes.paint((Graphics2D) g, visibleRect);
        buildTrackRenderer.paint((Graphics2D) g, null);
    }

    public void refreshAll() {
        background.refreshAll();
    }
}
