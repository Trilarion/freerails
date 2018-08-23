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

package freerails.move;

import freerails.model.world.UnmodifiableWorld;
import freerails.model.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

// TODO some orthogonality checks in the constructor, maybe with instanceof in something like MoveUtils.isOrthogonal(Move a, Move b) for all kind of combinations
/**
 * Combines a number of orthogonal moves. Orthogonal moves are moves that can be applied in any order (are not
 * dependent on each other). The validity of the composite move is determined by combining the validities of
 * each move.
 *
 * Must contain at least two moves.
 *
 * Please note that this class should not be used for dependent moves like building a long list of tracks.
 */
public class CompostMove implements Move {

    private final Collection<Move> moves;

    public CompostMove(@NotNull Collection<Move> moves) {
        if (moves.size() < 2) {
            throw new IllegalArgumentException("Must contain at least two moves.");
        }
        // defensive copy
        this.moves = Collections.unmodifiableCollection(new ArrayList<>(moves));
    }


    /**
     *
     * @param world
     * @return Either the first non-successful status or Status.OK
     */
    @Override
    public @NotNull Status applicable(@NotNull UnmodifiableWorld world) {
        for (Move move: moves) {
            Status status = move.applicable(world);
            if (!status.isSuccess()) {
                return status;
            }
        }
        return Status.OK;
    }

    @Override
    public @NotNull void apply(@NotNull World world) {
        for (Move move: moves) {
            move.apply(world);
        }

    }
}
