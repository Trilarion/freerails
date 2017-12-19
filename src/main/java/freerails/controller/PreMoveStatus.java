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

import freerails.move.MoveStatus;
import freerails.world.FreerailsSerializable;

/**
 * Records the success or failure of an attempt to execute a move.
 *
 */
final public class PreMoveStatus implements FreerailsSerializable {

    /**
     *
     */
    public static final PreMoveStatus PRE_MOVE_OK = new PreMoveStatus(
            MoveStatus.MOVE_OK);
    private static final long serialVersionUID = 3978145456646009140L;

    /**
     *
     */
    public final MoveStatus ms;

    private PreMoveStatus(MoveStatus ms) {
        this.ms = ms;
    }

    /**
     *
     * @param reason
     * @return
     */
    public static PreMoveStatus failed(String reason) {
        return new PreMoveStatus(MoveStatus.moveFailed(reason));
    }

    /**
     *
     * @param ms
     * @return
     */
    public static PreMoveStatus fromMoveStatus(MoveStatus ms) {
        if (ms.ok) {
            return PRE_MOVE_OK;
        }
        return new PreMoveStatus(ms);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PreMoveStatus))
            return false;

        final PreMoveStatus preMoveStatus = (PreMoveStatus) o;

        return ms.equals(preMoveStatus.ms);
    }

    @Override
    public int hashCode() {
        return ms.hashCode();
    }

    /**
     * Avoid creating a duplicate when deserializing.
     */
    private Object readResolve() {
        if (ms.ok) {
            return PRE_MOVE_OK;
        }
        return this;
    }
}