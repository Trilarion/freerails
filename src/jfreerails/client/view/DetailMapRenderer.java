package jfreerails.client.view;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import jfreerails.client.common.ModelRoot;
import jfreerails.client.common.Painter;
import jfreerails.client.renderer.MapBackgroundRender;
import jfreerails.client.renderer.MapLayerRenderer;
import jfreerails.client.renderer.MapRenderer;
import jfreerails.client.renderer.SquareTileBackgroundRenderer;
import jfreerails.client.renderer.StationBoxRenderer;
import jfreerails.client.renderer.StationRadiusRenderer;
import jfreerails.client.renderer.ViewLists;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.client.renderer.BuildTrackRenderer;


/** Draws the main map, that is the terrain, track, trains, station names etc.
 * @author Luke
 */
public class DetailMapRenderer implements MapRenderer {
    private static final boolean OSXWorkaround = (System.getProperty(
            "OSXWorkaround") != null);
    private final MapLayerRenderer background;
    private final Dimension mapSizeInPixels;
    private final OverHeadTrainView trainsview;
    private final StationRadiusRenderer stationRadius;
    private final BuildTrackRenderer buildTrack;
    private final Painter stationBoxes;

    public DetailMapRenderer(ReadOnlyWorld world, ViewLists vl,
        ModelRoot modelRoot) {
        trainsview = new OverHeadTrainView(world, vl);

        MapBackgroundRender render = new MapBackgroundRender(world, vl,
                modelRoot);

        if (OSXWorkaround) {
            //Don't buffer the mapviews background.
            background = render;
        } else {
            background = new SquareTileBackgroundRenderer(render);
        }

        Dimension mapSize = new Dimension(world.getMapWidth(),
                world.getMapHeight());
        mapSizeInPixels = new Dimension(mapSize.width * 30, mapSize.height * 30);
        stationRadius = new StationRadiusRenderer(modelRoot);
        buildTrack = new BuildTrackRenderer(world, vl.getTrackPieceViewList());
        stationBoxes = new StationBoxRenderer(world, vl, modelRoot);
    }

    public StationRadiusRenderer getStationRadius() {
        return stationRadius;
    }

    public BuildTrackRenderer getBuildTrack() {
        return buildTrack;
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
        stationRadius.paint((Graphics2D)g);
        stationBoxes.paint((Graphics2D)g);

        buildTrack.paint((Graphics2D)g);
    }

    public void refreshTile(int x, int y) {
        background.refreshTile(x, y);
    }

    public void paintRect(Graphics g, Rectangle visibleRect) {
        background.paintRect(g, visibleRect);
        trainsview.paint((Graphics2D)g);
        stationRadius.paint((Graphics2D)g);
        stationBoxes.paint((Graphics2D)g);
        buildTrack.paint((Graphics2D)g);
    }

    public void refreshAll() {
        background.refreshAll();
    }
}
