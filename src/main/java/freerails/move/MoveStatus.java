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

/**
 * Records the status or failure of an attempt to execute a move.
 */
public class MoveStatus implements Serializable {

    /**
     *
     */
    public static final MoveStatus MOVE_OK = new MoveStatus(true, "Move accepted");
    private static final long serialVersionUID = 3258129171879309624L;

    private final boolean status;
    private final String message;

    private MoveStatus(boolean status, String message) {
        this.status = status;
        this.message = message;
    }

    /**
     * @param reason
     * @return
     */
    public static MoveStatus moveFailed(String reason) {
        return new MoveStatus(false, reason);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MoveStatus)) return false;

        final MoveStatus moveStatus = (MoveStatus) obj;

        if (status != moveStatus.status) return false;
        return message != null ? message.equals(moveStatus.message) : moveStatus.message == null;
    }

    @Override
    public int hashCode() {
        int result;
        result = (status ? 1 : 0);
        result = 29 * result + (message != null ? message.hashCode() : 0);
        return result;
    }

    /**
     * Avoid creating a duplicate when deserializing.
     */
    private Object readResolve() {
        if (status) {
            return MOVE_OK;
        }
        return this;
    }

    /**
     * @return
     */
    public boolean succeeds() {
        return status;
    }

    @Override
    public String toString() {
        return message;
    }

    public String getMessage() {
        return message;
    }
}