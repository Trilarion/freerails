package jfreerails.client.view;

import java.awt.Point;
import java.awt.Rectangle;

import jfreerails.client.renderer.MapRenderer;
import jfreerails.controller.MoveReceiver;
import jfreerails.move.MapUpdateMove;
import jfreerails.move.Move;

public class MapViewMoveReceiver implements MoveReceiver {

	private final MapRenderer mapView;

	public MapViewMoveReceiver(MapRenderer mv) {
		mapView=mv;
	}

	public void processMove(Move move) {		

		if (move instanceof MapUpdateMove) {
		
			Rectangle r = ((MapUpdateMove) move).getUpdatedTiles();
		
			Point tile = new Point();
			for (tile.x = r.x; tile.x < (r.x + r.width); tile.x++) {
				for (tile.y = r.y; tile.y < (r.y + r.height); tile.y++) {
					
					mapView.refreshTile(tile.x, tile.y);

				}
			}

		}
	}

}
