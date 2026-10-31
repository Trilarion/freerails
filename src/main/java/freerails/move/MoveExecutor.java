/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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

package freerails.move;

import freerails.move.generator.MoveGenerator;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.player.Player;

/**
 * Lets the caller try and execute Moves.
 */
public interface MoveExecutor {

    /**
     * @param move the move to apply or process
     * @return a status describing whether the move was applied
     */
    Status applyMove(Move move);

    /**
     * @param moveGenerator the generator associated with or used to create the move
     * @return the status returned by pre-move processing
     */
    Status doPreMove(MoveGenerator moveGenerator);

    /**
     * @param move the move to apply or process
     * @return the result of validating the move before application
     */
    Status tryMove(Move move);

    /**
     * @return the read-only game world managed by this executor
     */
    UnmodifiableWorld getWorld();

    /**
     * @return the player associated with this executor
     */
    Player getPlayer();
}