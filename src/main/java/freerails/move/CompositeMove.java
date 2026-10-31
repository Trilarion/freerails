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

/*
 *
 */
package freerails.move;

import freerails.model.world.UnmodifiableWorld;
import freerails.util.Utils;
import freerails.model.world.World;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.util.List;

/**
 * This Move may be subclassed to create a move composed of a number of
 * component Moves where atomicity of the move is required. This class defines a
 * number of methods which may not be subclassed - all changes must be
 * encapsulated as sub-moves of this move.
 */
public class CompositeMove implements Move {

    @Serial
    private static final long serialVersionUID = 3257289149391517489L;
    private final List<Move> moves;

    /**
     * Creates a composite move that applies the supplied moves in order.
     *
     * @param moves the moves to execute in order
     */
    public CompositeMove(List<Move> moves) {
        this.moves = Utils.immutableList(moves);
    }

    /**
     * Returns whether this object is equal to the specified object.
     *
     * @param obj the object to compare with this object
     * @return true if this object is equal to the specified object, otherwise false
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        return moves.equals(((CompositeMove) obj).moves);
    }

    /**
     * Returns the move at the specified position in this composite move.
     *
     * @param i the zero-based position of the move
     * @return the move at the specified position
     */
    public final Move getMove(int i) {
        return moves.get(i);
    }

    /**
     * Returns a hash code for this object.
     *
     * @return the hash code for this object
     */
    @Override
    public int hashCode() {
        return moves.hashCode();
    }

    /**
     * Returns the component moves in their application order.
     *
     * @return the immutable list of component moves
     */
    public final List<Move> getMoves() {
        return moves;
    }

    /**
     * Checks whether this move can be applied to the supplied world.
     *
     * @param world the world whose state is used or updated
     * @return a status indicating whether the move can be applied
     */
    @NotNull
    @Override
    public Status applicable(@NotNull UnmodifiableWorld world) {
        for (Move move : moves) {
            Status status = move.applicable(world);
            if (!status.isSuccess()) {
                return status;
            }
        }

        if (world instanceof World) {
            return compositeTest((World) world);
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
        Status status = applicable(world);

        if (!status.isSuccess()) {
            return;
        }

        for (Move move : moves) {
            move.apply(world);
        }
    }

    /**
     * Subclasses may override this method to perform tests which pass or fail
     * depending on the combination of moves making up this composite move.
     *
     * @param world the mutable world used for combination-specific checks
     * @return a status describing whether the composite move passes those checks
     */
    public Status compositeTest(World world) {
        return Status.OK;
    }

    /**
     * Returns a string representation of this object.
     *
     * @return the string
     */
    @Override
    public final String toString() {
        StringBuilder s = new StringBuilder();

        for (int i = 0; i < moves.size(); i++) {
            s.append(moves.get(i).toString()).append(i > 0 ? ", " : "");
        }

        return s.toString();
    }
}