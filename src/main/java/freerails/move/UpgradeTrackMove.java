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
package freerails.move;

import freerails.util.Point2D;
import freerails.world.track.TrackPiece;

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
    public static UpgradeTrackMove generateMove(TrackPiece before,
                                                TrackPiece after, Point2D p) {
        ChangeTrackPieceMove m = new ChangeTrackPieceMove(before, after, p);

        return new UpgradeTrackMove(m);
    }

    /**
     * @return
     */
    public Rectangle getUpdatedTiles() {
        ChangeTrackPieceMove m = (ChangeTrackPieceMove) this.getMove(0);

        return m.getUpdatedTiles();
    }
}