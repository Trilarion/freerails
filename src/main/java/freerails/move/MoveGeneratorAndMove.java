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

import java.io.Serializable;

// TODO remove this, only used in MovePrecommitter

/**
 * Associates a move with the generator that produced or controls it.

 * @param move the move associated with the generator
 * @param moveGenerator the generator associated with the move
 */
public record MoveGeneratorAndMove(Move move, MoveGenerator moveGenerator) implements Serializable {

    private static final long serialVersionUID = 3256443607635342897L;

    /**
     * Creates a move-generator association with either value optionally absent.
     *
     * @param moveGenerator the generator associated with the move, or {@code null}
     * @param move the move associated with the generator, or {@code null}
     */
    public MoveGeneratorAndMove(MoveGenerator moveGenerator, Move move) {
        this(move, moveGenerator);
    }

}
