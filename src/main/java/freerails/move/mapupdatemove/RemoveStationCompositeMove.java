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

import freerails.move.CompostMove;
import freerails.move.Move;

import java.awt.*;
import java.util.List;

// TODO are these really orthogonal moves?, I think not, make it a single piece
/**
 * This move removes a station from the station list and from the map.
 */
public class RemoveStationCompositeMove extends CompostMove implements TrackMove {

    private static final long serialVersionUID = 3760847865429702969L;

    private final Rectangle rectangle;

    public RemoveStationCompositeMove(List<Move> moves) {
        super(moves);
        rectangle =  ((MapUpdateMove) moves.get(0)).getUpdatedTiles();
    }

    /**
     * @return
     */
    @Override
    public Rectangle getUpdatedTiles() {
        return rectangle;
    }
}