/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 *
 */
package freerails.move.mapupdatemove;

import freerails.move.CompositeMove;
import freerails.move.Move;
import freerails.util.Vec2D;
import freerails.model.track.TrackPiece;

import java.awt.*;

/**
 * This CompositeMove changes the track type at a point on the map and charges
 * the players account for the cost of the change.
 */
public class UpgradeTrackMove extends CompositeMove implements TrackMove {

    private static final long serialVersionUID = 3907215961470875442L;

    private UpgradeTrackMove(ChangeTrackPieceMove trackMove) {
        super(trackMove);
    }

    /**
     * @param before
     * @param after
     * @param p
     * @return
     */
    public static Move generateMove(TrackPiece before, TrackPiece after, Vec2D p) {
        ChangeTrackPieceMove changeTrackPieceMove = new ChangeTrackPieceMove(before, after, p);

        return new UpgradeTrackMove(changeTrackPieceMove);
    }

    /**
     * @return
     */
    public Rectangle getUpdatedTiles() {
        MapUpdateMove mapUpdateMove = (ChangeTrackPieceMove) getMove(0);

        return mapUpdateMove.getUpdatedTiles();
    }
}