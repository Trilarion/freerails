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

    public void tilesChanged(Rectangle tilesChanged) {
        Point tile = new Point();

        for (tile.x = tilesChanged.x;
                tile.x < (tilesChanged.x + tilesChanged.width); tile.x++) {
            for (tile.y = tilesChanged.y;
                    tile.y < (tilesChanged.y + tilesChanged.height);
                    tile.y++) {
                mapView.refreshTile(tile.x, tile.y);
            }
        }
    }
}