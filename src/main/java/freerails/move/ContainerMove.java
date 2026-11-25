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

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A move made of component moves, such as an ordered {@link CompositeMove} or an orthogonal
 * {@link CompostMove}. It gives read-only access to the components so that receivers can inspect
 * them without knowing which kind of container they are in.
 */
public interface ContainerMove extends Move {

    /**
     * Returns the component moves.
     *
     * @return an unmodifiable list of the component moves
     */
    @NotNull
    List<Move> getMoves();

    /**
     * Returns the moves that are not containers, found by descending into every nested container
     * in order. A move that is not a container is returned on its own.
     *
     * @param move the move to flatten
     * @return an unmodifiable list of the non-container moves
     */
    static @NotNull List<Move> leaves(@NotNull Move move) {
        List<Move> leaves = new ArrayList<>();
        collectLeaves(move, leaves);
        return List.copyOf(leaves);
    }

    /**
     * Adds the non-container moves of the supplied move to the list.
     *
     * @param move the move to descend into
     * @param leaves the list that receives the non-container moves
     */
    private static void collectLeaves(Move move, List<Move> leaves) {
        if (move instanceof ContainerMove container) {
            for (Move component : container.getMoves()) {
                collectLeaves(component, leaves);
            }
        } else {
            leaves.add(move);
        }
    }
}
