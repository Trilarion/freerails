/*
 * Copyright (C) 2002 Luke Lindsay
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

/*
 * ChangeTrackPieceCompositeMove.java
 *
 * Created on 25 January 2002, 23:49
 */
package org.railz.move;

import java.util.ArrayList;
import java.awt.Point;
import java.awt.Rectangle;

import org.railz.world.building.*;
import org.railz.world.common.*;
import org.railz.world.player.FreerailsPrincipal;
import org.railz.world.player.Player;
import org.railz.world.top.*;
import org.railz.world.track.*;

/**
 * This Move changes adds, removes, or upgrades the track between two tiles.
 * @author  lindsal
 *
 */
public final class ChangeTrackPieceCompositeMove extends CompositeMove
    implements TrackMove, MapUpdateMove {
    private final Rectangle updatedTiles;

    /**
     * Creates new ChangeTrackPieceCompositeMove
     */
    private ChangeTrackPieceCompositeMove(TrackMove a, TrackMove b) {
        this(new Move[0], a, b);
    }

    private static Move[] createMoves(Move[] moves, TrackMove a, TrackMove b) {
	int i;
	Move[] m = new Move[moves.length + 2];
	for (i = 0; i < moves.length; i++)
	    m[i] = moves[i];

	m[i++] = a;
	m[i++] = b;
	return m;
    }

    private ChangeTrackPieceCompositeMove(Move[] moves, TrackMove a, TrackMove
	    b) {
	super(createMoves(moves, a, b));
	updatedTiles = a.getUpdatedTiles().union(b.getUpdatedTiles());
    }

    public static ChangeTrackPieceCompositeMove generateBuildTrackMove(
        Point from, byte direction, int trackRule,
        ReadOnlyWorld w, FreerailsPrincipal p) {
	// Check to see whether we need to purchase land on either of the two
	// connected tiles
	Point to = new Point (from.x + CompassPoints.getUnitDeltaX(direction),
		from.y + CompassPoints.getUnitDeltaY(direction));
	ArrayList moves = new ArrayList();
	if (w.getTile(from.x, from.y).getOwner().equals(Player.AUTHORITATIVE))
	    moves.add(new PurchaseTileMove(w, from, p));

	if (w.getTile(to.x, to.y).getOwner().equals(Player.AUTHORITATIVE))
	    moves.add(new PurchaseTileMove(w, to, p));

        ChangeTrackPieceMove a;
        ChangeTrackPieceMove b;

        a = getBuildTrackChangeTrackPieceMove(from, direction, trackRule, w, p);
	b = getBuildTrackChangeTrackPieceMove(to,
		CompassPoints.invert(direction), trackRule, w, p);

        return new ChangeTrackPieceCompositeMove((Move []) moves.toArray(new
		Move[moves.size()]), a, b);
    }

    public static ChangeTrackPieceCompositeMove generateRemoveTrackMove(
        Point from, byte direction, ReadOnlyWorld w,
	FreerailsPrincipal p) {
        TrackMove a;
        TrackMove b;

        a = getRemoveTrackChangeTrackPieceMove(from, direction, w, p);
        b = getRemoveTrackChangeTrackPieceMove
	    (new Point(from.x + CompassPoints.getUnitDeltaX(direction),
		       from.y + CompassPoints.getUnitDeltaY(direction)),
	     CompassPoints.invert(direction), w, p);

        return new ChangeTrackPieceCompositeMove(a, b);
    }

    //utility method.
    private static ChangeTrackPieceMove getBuildTrackChangeTrackPieceMove(
        Point p, byte direction, int trackRule,
        ReadOnlyWorld w, FreerailsPrincipal owner) {
        TrackTile oldTrackPiece;
        TrackTile newTrackPiece;

        if (w.boundsContain(p.x, p.y)) {
            oldTrackPiece = w.getTile(p.x, p.y).getTrackTile();

            if (oldTrackPiece != null) {
		byte trackConfiguration =
		    (byte) (oldTrackPiece.getTrackConfiguration() | direction);
		newTrackPiece = TrackTile.createTrackTile(w,
			trackConfiguration, oldTrackPiece.getTrackRule());
            } else {
		newTrackPiece = TrackTile.createTrackTile(w, direction,
			trackRule);
            }
        } else {
            newTrackPiece = TrackTile.createTrackTile(w, direction, trackRule);
            oldTrackPiece = null;
        }

        return new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece, p, owner);
    }

    //utility method.
    private static TrackMove getRemoveTrackChangeTrackPieceMove(Point p,
        byte direction, ReadOnlyWorld w, FreerailsPrincipal owner) {
        TrackTile oldTrackPiece;
        TrackTile newTrackPiece;

        if (w.boundsContain(p.x, p.y)) {
            oldTrackPiece = w.getTile(p.x, p.y).getTrackTile();

            if (oldTrackPiece != null) {
		byte trackConfiguration =
		    (byte) (oldTrackPiece.getTrackConfiguration() & ~direction);

                if (trackConfiguration != 0) {
		    newTrackPiece = TrackTile.createTrackTile(w,
			    trackConfiguration, oldTrackPiece.getTrackRule());
                } else {
                    newTrackPiece = null;
                }
            } else {
                newTrackPiece = null;
            }
        } else {
            newTrackPiece = null;
            oldTrackPiece = null;
        }

        ChangeTrackPieceMove m = new ChangeTrackPieceMove(oldTrackPiece,
                newTrackPiece, p, owner);

	//If we are removing a station, we also need to remove the station
	//from the staiton list.
	BuildingTile bt = w.getTile(p).getBuildingTile();
        if (bt != null && ((BuildingType) w.get(KEY.BUILDING_TYPES,
		       	bt.getType(), Player.AUTHORITATIVE)).getCategory()
	       	== BuildingType.CATEGORY_STATION) {
            return RemoveStationMove.getInstance(w, m, owner);
        } else {
            return m;
        }
    }

    public Rectangle getUpdatedTiles() {
        return updatedTiles;
    }
}
