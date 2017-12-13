/*
 * Copyright (C) 2003 Luke Lindsay
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
 * Created on 21-Jul-2003
 *
 */
package jfreerails.move;

import java.awt.Point;
import java.awt.Rectangle;

import jfreerails.world.track.TrackPiece;
import jfreerails.world.player.FreerailsPrincipal;

/**
 * This CompositeMove changes the track type at a point
 * on the map and charges the players account for the cost
 * of the change.
 *
 * @author Luke Lindsay
 *
 */
public class UpgradeTrackMove extends CompositeMove implements TrackMove {
    public UpgradeTrackMove(ChangeTrackPieceMove trackMove) {
        super(new Move[] {trackMove});
    }

    public static UpgradeTrackMove generateMove(TrackPiece before,
        TrackPiece after, Point p, FreerailsPrincipal trackOwner) {
        ChangeTrackPieceMove m = new ChangeTrackPieceMove(before, after, p,
		trackOwner);

        return new UpgradeTrackMove(m);
    }

    public Rectangle getUpdatedTiles() {
        ChangeTrackPieceMove m = (ChangeTrackPieceMove)this.getMove(0);

        return m.getUpdatedTiles();
    }
}
