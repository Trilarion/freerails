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

import java.io.Serializable;

// TODO not much difference to MoveStatus, maybe merge the two
/**
 * Records the success or failure of an attempt to execute a move.
 */
public class TryMoveStatus implements Serializable {

    public static final TryMoveStatus TRY_MOVE_OK = new TryMoveStatus(Status.OK);
    private static final long serialVersionUID = 3978145456646009140L;
    public final Status status;

    private TryMoveStatus(Status status) {
        this.status = status;
    }

    /**
     * @param reason
     * @return
     */
    public static TryMoveStatus failed(String reason) {
        return new TryMoveStatus(Status.moveFailed(reason));
    }

    /**
     * @param status
     * @return
     */
    public static Serializable fromMoveStatus(Status status) {
        if (status.succeeds()) {
            return TRY_MOVE_OK;
        }
        return new TryMoveStatus(status);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TryMoveStatus)) return false;

        final TryMoveStatus tryMoveStatus = (TryMoveStatus) obj;

        return status.equals(tryMoveStatus.status);
    }

    @Override
    public int hashCode() {
        return status.hashCode();
    }

    /**
     * Avoid creating a duplicate when deserializing.
     */
    private Object readResolve() {
        if (status.succeeds()) {
            return TRY_MOVE_OK;
        }
        return this;
    }
}