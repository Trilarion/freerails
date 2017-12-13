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
package jfreerails.move;

import java.util.ArrayList;
import java.awt.Point;
import java.awt.Rectangle;

import jfreerails.world.common.OneTileMoveVector;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.player.Player;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.track.FreerailsTile;
import jfreerails.world.track.NullTrackPiece;
import jfreerails.world.track.NullTrackType;
import jfreerails.world.track.TrackConfiguration;
import jfreerails.world.track.TrackPiece;
import jfreerails.world.track.TrackRule;


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
        Point from, OneTileMoveVector direction, TrackRule trackRule,
        ReadOnlyWorld w, FreerailsPrincipal p) {
	// Check to see whether we need to purchase land on either of the two
	// connected tiles
	Point to = direction.createRelocatedPoint(from);
	ArrayList moves = new ArrayList();
	if (w.getTile(from.x, from.y).getOwner().equals(Player.AUTHORITATIVE))
	    moves.add(new PurchaseTileMove(w, from, p));

	if (w.getTile(to.x, to.y).getOwner().equals(Player.AUTHORITATIVE))
	    moves.add(new PurchaseTileMove(w, to, p));

        ChangeTrackPieceMove a;
        ChangeTrackPieceMove b;

        a = getBuildTrackChangeTrackPieceMove(from, direction, trackRule, w, p);
	b = getBuildTrackChangeTrackPieceMove(to, direction.getOpposite(),
		trackRule, w, p);

        return new ChangeTrackPieceCompositeMove((Move []) moves.toArray(new
		Move[moves.size()]), a, b);
    }

    public static ChangeTrackPieceCompositeMove generateRemoveTrackMove(
        Point from, OneTileMoveVector direction, ReadOnlyWorld w,
	FreerailsPrincipal p) {
        TrackMove a;
        TrackMove b;

        a = getRemoveTrackChangeTrackPieceMove(from, direction, w, p);
        b = getRemoveTrackChangeTrackPieceMove(direction.createRelocatedPoint(
                    from), direction.getOpposite(), w, p);

        return new ChangeTrackPieceCompositeMove(a, b);
    }

    //utility method.
    private static ChangeTrackPieceMove getBuildTrackChangeTrackPieceMove(
        Point p, OneTileMoveVector direction, TrackRule trackRule,
        ReadOnlyWorld w, FreerailsPrincipal owner) {
        TrackPiece oldTrackPiece;
        TrackPiece newTrackPiece;

        if (w.boundsContain(p.x, p.y)) {
            oldTrackPiece = ((FreerailsTile)w.getTile(p.x, p.y)).getTrackPiece();

            if (oldTrackPiece.getTrackRule() != NullTrackType.getInstance()) {
                TrackConfiguration trackConfiguration = TrackConfiguration.add(oldTrackPiece.getTrackConfiguration(),
                        direction);
                newTrackPiece = oldTrackPiece.getTrackRule().getTrackPiece(trackConfiguration);
            } else {
                newTrackPiece = getTrackPieceWhenOldTrackPieceIsNull(direction,
                        trackRule);
            }
        } else {
            newTrackPiece = getTrackPieceWhenOldTrackPieceIsNull(direction,
                    trackRule);
            oldTrackPiece = NullTrackPiece.getInstance();
        }

        return new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece, p, owner);
    }

    //utility method.
    private static TrackMove getRemoveTrackChangeTrackPieceMove(Point p,
        OneTileMoveVector direction, ReadOnlyWorld w, FreerailsPrincipal owner) {
        TrackPiece oldTrackPiece;
        TrackPiece newTrackPiece;

        if (w.boundsContain(p.x, p.y)) {
            oldTrackPiece = (TrackPiece)w.getTile(p.x, p.y);

            if (oldTrackPiece.getTrackRule() != NullTrackType.getInstance()) {
                TrackConfiguration trackConfiguration = TrackConfiguration.subtract(oldTrackPiece.getTrackConfiguration(),
                        direction);

                if (trackConfiguration != TrackConfiguration.getFlatInstance(
                            "000010000")) {
                    newTrackPiece = oldTrackPiece.getTrackRule().getTrackPiece(trackConfiguration);
                } else {
                    newTrackPiece = NullTrackPiece.getInstance();
                }
            } else {
                newTrackPiece = NullTrackPiece.getInstance();
            }
        } else {
            newTrackPiece = NullTrackPiece.getInstance();
            oldTrackPiece = NullTrackPiece.getInstance();
        }

        ChangeTrackPieceMove m = new ChangeTrackPieceMove(oldTrackPiece,
                newTrackPiece, p, owner);

        //If we are removing a station, we also need to remove the station from the staiton list.
        if (oldTrackPiece.getTrackRule().isStation() &&
                !newTrackPiece.getTrackRule().isStation()) {
            return RemoveStationMove.getInstance(w, m, owner);
        } else {
            return m;
        }
    }

    private static TrackPiece getTrackPieceWhenOldTrackPieceIsNull(
        OneTileMoveVector direction, TrackRule trackRule) {
        TrackConfiguration simplestConfig = TrackConfiguration.getFlatInstance(
                "000010000");
        TrackConfiguration trackConfiguration = TrackConfiguration.add(simplestConfig,
                direction);

        return trackRule.getTrackPiece(trackConfiguration);
    }

    public Rectangle getUpdatedTiles() {
        return updatedTiles;
    }
}
