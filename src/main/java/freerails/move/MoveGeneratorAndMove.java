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

import freerails.move.generator.MoveGenerator;

import java.io.Serializable;

// TODO remove this, only used in MovePrecommitter
/**
 *
 */
public class MoveGeneratorAndMove implements Serializable {

    private static final long serialVersionUID = 3256443607635342897L;
    public final Move move;
    public final MoveGenerator moveGenerator;

    /**
     * Both parameters could be null.
     *
     * @param moveGenerator
     * @param move
     */
    public MoveGeneratorAndMove(MoveGenerator moveGenerator, Move move) {
        this.move = move;
        this.moveGenerator = moveGenerator;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MoveGeneratorAndMove)) return false;

        final MoveGeneratorAndMove preMoveAndMove = (MoveGeneratorAndMove) obj;

        if (move != null ? !move.equals(preMoveAndMove.move) : preMoveAndMove.move != null) return false;
        return moveGenerator != null ? moveGenerator.equals(preMoveAndMove.moveGenerator) : preMoveAndMove.moveGenerator == null;
    }

    @Override
    public int hashCode() {
        int result;
        result = move != null ? move.hashCode() : 0;
        result = 29 * result + (moveGenerator != null ? moveGenerator.hashCode() : 0);
        return result;
    }
}
