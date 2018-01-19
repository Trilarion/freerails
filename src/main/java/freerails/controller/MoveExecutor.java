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

package freerails.controller;

import freerails.move.Move;
import freerails.move.MoveStatus;
import freerails.move.PreMove;
import freerails.world.ReadOnlyWorld;
import freerails.world.player.FreerailsPrincipal;

/**
 * Lets the caller try and execute Moves.
 */
public interface MoveExecutor {

    /**
     * @param move
     * @return
     */
    MoveStatus doMove(Move move);

    /**
     * @param preMove
     * @return
     */
    MoveStatus doPreMove(PreMove preMove);

    /**
     * @param move
     * @return
     */
    MoveStatus tryDoMove(Move move);

    /**
     * @return
     */
    ReadOnlyWorld getWorld();

    /**
     * @return
     */
    FreerailsPrincipal getPrincipal();
}