package jfreerails.client.view;

import java.awt.Point;
import java.awt.Rectangle;

import jfreerails.move.MapUpdateMove;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.controller.MoveReceiver;

public class MapViewMoveReceiver implements MoveReceiver {

	private final MapView mapView;

	private Class mapUpdateMoveClass;

	public MapViewMoveReceiver(MapView mv) {
		mapView=mv;

		try {
			mapUpdateMoveClass = Class.forName("jfreerails.move.MapUpdateMove");

		} catch (ClassNotFoundException e) {

			e.printStackTrace();
		}
	}

	public MoveStatus processMove(Move move) {
		if (mapUpdateMoveClass.isInstance(move)) {
		
			Rectangle r = ((MapUpdateMove) move).getUpdatedTiles();
		
			Point tile = new Point();
			for (tile.x = r.x; tile.x < (r.x + r.width); tile.x++) {
				for (tile.y = r.y; tile.y < (r.y + r.height); tile.y++) {
					
					mapView.refreshTile(tile.x, tile.y);

				}
			}

		}
		return MoveStatus.MOVE_RECEIVED;

	}

}
