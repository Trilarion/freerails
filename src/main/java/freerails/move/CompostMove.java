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

import freerails.model.world.UnmodifiableWorld;
import freerails.model.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Combines a number of orthogonal moves. Orthogonal moves are moves that can be applied in any order (are not
 * dependent on each other). Each component move must be applicable to the same original world, and applying
 * the component moves in any order must produce equivalent game state. The constructor does not check this
 * semantic requirement; callers are responsible for combining only orthogonal moves.
 * <p>
 * Must contain at least two moves.
 * <p>
 * Do not use this class for dependent moves, such as changes to connected track pieces. Use an ordered
 * {@link CompositeMove} for dependent moves.
 */
public class CompostMove implements Move {

    private final Collection<Move> moves;

    /**
     * Creates a compost move from the supplied values.
     *
     * @param moves the moves
     */
    public CompostMove(@NotNull Collection<Move> moves) {
        if (moves.size() < 2) {
            throw new IllegalArgumentException("Must contain at least two moves.");
        }
        for (Move move: moves) {
            if (move instanceof CompostMove) {
                throw new IllegalArgumentException("No nested composite moves.");
            }
        }
        // defensive copy
        this.moves = Collections.unmodifiableCollection(new ArrayList<>(moves));
    }


    /**
     * Compares this composite move with another object.
     *
     * @param obj object to compare with
     * @return {@code true} if the object is an equivalent composite move
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        return new ArrayList<>(moves).equals(new ArrayList<>(((CompostMove) obj).moves));
    }

    /**
     * Returns a hash code for this object.
     *
     * @return the hash code for this object
     */
    @Override
    public int hashCode() {
        return new ArrayList<>(moves).hashCode();
    }

    /**
     * Checks whether this move can be applied to the supplied world.
     *
     * @param world the world whose state is used or updated
     * @return a status indicating whether the move can be applied
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

    /**
     * Applies this move to the supplied world.
     *
     * @param world the world whose state is used or updated
     */
    @Override
    public void apply(@NotNull World world) {
        if (!applicable(world).isSuccess()) {
            return;
        }

        for (Move move: moves) {
            move.apply(world);
        }
    }
}
