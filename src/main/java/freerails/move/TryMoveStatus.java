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

import java.io.Serial;
import java.io.Serializable;

/**
 * Records the success or failure of an attempt to execute a move.
 */
public class TryMoveStatus implements Serializable {

    public static final TryMoveStatus TRY_MOVE_OK = new TryMoveStatus(Status.OK);
    @Serial
    private static final long serialVersionUID = 3978145456646009140L;
    public final Status status;

    /**
     * Creates a try move status from the supplied values.
     *
     * @param status the status
     */
    private TryMoveStatus(Status status) {
        this.status = status;
    }

    /**
     * @param reason the explanation for the failed move attempt
     * @return a failed move status containing the supplied reason
     */
    public static TryMoveStatus failed(String reason) {
        return new TryMoveStatus(Status.fail(reason));
    }

    /**
     * @param status the move status to convert to a serializable result
     * @return a serializable representation of the supplied move status
     */
    public static Serializable fromMoveStatus(Status status) {
        if (status.isSuccess()) {
            return TRY_MOVE_OK;
        }
        return new TryMoveStatus(status);
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
        if (!(obj instanceof TryMoveStatus tryMoveStatus)) return false;

        return status.equals(tryMoveStatus.status);
    }

    /**
     * Returns a hash code for this object.
     *
     * @return the hash code for this object
     */
    @Override
    public int hashCode() {
        return status.hashCode();
    }

    /**
     * Avoid creating a duplicate when deserializing.
     */
    @Serial
    private Object readResolve() {
        if (status.isSuccess()) {
            return TRY_MOVE_OK;
        }
        return this;
    }
}