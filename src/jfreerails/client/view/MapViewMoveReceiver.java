package jfreerails.client.view;

import java.awt.Point;
import java.awt.Rectangle;
import jfreerails.client.renderer.MapRenderer;
import jfreerails.world.top.WorldMapListener;


/** Listens for changes on the map, for instance when track is built, and
 * refreshes the map view.
 * @author Luke
 */
public class MapViewMoveReceiver implements WorldMapListener {
    private final MapRenderer mapView;

    public MapViewMoveReceiver(MapRenderer mv) {
        mapView = mv;
    }

    public void tilesChanged(Rectangle r) {
        Point tile = new Point();

        for (tile.x = r.x; tile.x < (r.x + r.width); tile.x++) {
            for (tile.y = r.y; tile.y < (r.y + r.height); tile.y++) {
                mapView.refreshTile(tile.x, tile.y);
            }
        }
    }
}