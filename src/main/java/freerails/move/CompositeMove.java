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

import freerails.model.world.UnmodifiableWorld;
import freerails.util.Utils;
import freerails.model.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * This Move may be subclassed to create a move composed of a number of
 * component Moves where atomicity of the move is required. This class defines a
 * number of methods which may not be subclassed - all changes must be
 * encapsulated as sub-moves of this move.
 */
public class CompositeMove implements Move {

    private static final long serialVersionUID = 3257289149391517489L;
    private final List<Move> moves;

    /**
     * @param moves
     */
    public CompositeMove(List<Move> moves) {
        this.moves = Utils.immutableList(moves);
    }

    public CompositeMove(Move move, List<Move> moreMoves) {
        moves = new ArrayList<>();
        moves.add(move);
        moves.addAll(moreMoves);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof CompositeMove)) return false;

        final CompositeMove compositeMove = (CompositeMove) obj;

        return moves.equals(compositeMove.moves);
    }

    /**
     * This method lets sub classes look at the moves.
     */
    public final Move getMove(int i) {
        return moves.get(i);
    }

    @Override
    public int hashCode() {
        // This will do for now.
        return moves.size();
    }

    /**
     * @return
     */
    public final List<Move> getMoves() {
        return moves;
    }

    @Override
    public Status applicable(UnmodifiableWorld world) {
        // TODO need to implement in a less general way
        // return Status.fail("currently not implemented");
        return Status.OK;
    }

    @Override
    public void apply(World world) {
        Status status = compositeTest(world);

        if (!status.isSuccess()) {
            throw new RuntimeException(status.getMessage());
        }

        for (int i = 0; i < moves.size(); i++) {
            moves.get(i).apply(world);
        }
    }

    /**
     * Subclasses may override this method to perform tests which pass or fail
     * depending on the combination of moves making up this composite move.
     */
    public Status compositeTest(World world) {
        return Status.OK;
    }

    /**
     * @return
     */
    public int size() {
        return moves.size();
    }

    @Override
    public final String toString() {
        StringBuilder s = new StringBuilder();

        for (int i = 0; i < moves.size(); i++) {
            s.append(moves.get(i).toString()).append((i > 0) ? ", " : "");
        }

        return s.toString();
    }
}