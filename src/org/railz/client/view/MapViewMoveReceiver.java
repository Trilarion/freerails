/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.railz.client.view;

import java.awt.Point;
import java.awt.Rectangle;

import org.railz.client.renderer.MapRenderer;
import org.railz.controller.MoveReceiver;
import org.railz.move.MapUpdateMove;
import org.railz.move.Move;
import org.railz.move.UndoneMove;

public class MapViewMoveReceiver implements MoveReceiver {

    private final MapRenderer mapView;

    private Class mapUpdateMoveClass;

    public MapViewMoveReceiver(MapRenderer mv) {
	mapView=mv;

	try {
	    mapUpdateMoveClass = Class.forName("org.railz.move.MapUpdateMove");

	} catch (ClassNotFoundException e) {

	    e.printStackTrace();
	}
    }

    public void processMove(Move move) {
	if (move instanceof UndoneMove) {
	    move = ((UndoneMove) move).getUndoneMove();
	}

	if (mapUpdateMoveClass.isInstance(move)) {

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
