package jfreerails.client.view;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import jfreerails.client.renderer.CityNamesRenderer;
import jfreerails.client.renderer.MapBackgroundRender;
import jfreerails.client.renderer.MapLayerRenderer;
import jfreerails.client.renderer.MapRenderer;
import jfreerails.client.renderer.SquareTileBackgroundRenderer;
import jfreerails.client.renderer.StationNamesRenderer;
import jfreerails.client.renderer.StationRadiusRenderer;
import jfreerails.client.renderer.ViewLists;
import jfreerails.client.common.Painter;
import jfreerails.world.top.ReadOnlyWorld;


public class DetailMapView implements MapRenderer {
    private static boolean OSXWorkaround = (System.getProperty("OSXWorkaround") != null);
    private final MapLayerRenderer background;
    private final Dimension mapSizeInPixels;
    private final TestOverHeadTrainView trainsview;
    private final Painter cityNames;
    private final Painter stationNames;
    private final StationRadiusRenderer stationRadius;

    public DetailMapView(ReadOnlyWorld world, ViewLists vl) {
        trainsview = new TestOverHeadTrainView(world, vl);

        if (OSXWorkaround) {
            //Don't buffer the mapviews background.
            background = new MapBackgroundRender(world, vl.getTileViewList(),
                    vl.getTrackPieceViewList());
        } else {
            background = new SquareTileBackgroundRenderer(new MapBackgroundRender(
                        world, vl.getTileViewList(), vl.getTrackPieceViewList()),
                    30);
        }

        Dimension mapSize = new Dimension(world.getMapWidth(),
                world.getMapHeight());
        mapSizeInPixels = new Dimension(mapSize.width * 30, mapSize.height * 30);

        cityNames = new CityNamesRenderer(world);
        stationNames = new StationNamesRenderer(world);
        stationRadius = new StationRadiusRenderer();
    }

    public StationRadiusRenderer getStationRadius() {
        return stationRadius;
    }

    public float getScale() {
        return 30;
    }

    public Dimension getMapSizeInPixels() {
        return mapSizeInPixels;
    }

    public void paintTile(Graphics g, int tileX, int tileY) {
        background.paintTile(g, tileX, tileY);
        trainsview.paint((Graphics2D)g);
        cityNames.paint((Graphics2D)g);
        stationNames.paint((Graphics2D)g);
        stationRadius.paint((Graphics2D)g);
    }

    public void refreshTile(int x, int y) {
        background.refreshTile(x, y);
    }

    /**
     * @param g Graphics context with origin pointing to map origin.
     * @param visibleRect rectangle defining area of map to draw relative to
     * origin 0,0 at top left of map, measured in pixels.
     */
    public void paintRect(Graphics g, Rectangle visibleRect) {
        background.paintRect(g, visibleRect);
        trainsview.paint((Graphics2D)g);
        cityNames.paint((Graphics2D)g);
        stationNames.paint((Graphics2D)g);
        stationRadius.paint((Graphics2D)g);
    }
}
