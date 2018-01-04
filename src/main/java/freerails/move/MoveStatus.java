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

import freerails.util.Immutable;

import java.io.Serializable;

/**
 * Records the success or failure of an attempt to execute a move.
 */
@Immutable
public final class MoveStatus implements Serializable {

    /**
     *
     */
    public static final MoveStatus MOVE_OK = new MoveStatus(true, "Move accepted");
    private static final long serialVersionUID = 3258129171879309624L;

    /**
     *
     */
    public final boolean ok;

    /**
     *
     */
    public final String message;

    private MoveStatus(boolean ok, String message) {
        Throwable t;
        if (ok) {
            t = null;
        } else {
            t = new Throwable();
            t.fillInStackTrace();
        }
        this.ok = ok;
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
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof MoveStatus))
            return false;

        final MoveStatus moveStatus = (MoveStatus) o;

        if (ok != moveStatus.ok)
            return false;
        return message != null ? message.equals(moveStatus.message) : moveStatus.message == null;
    }

    @Override
    public int hashCode() {
        int result;
        result = (ok ? 1 : 0);
        result = 29 * result + (message != null ? message.hashCode() : 0);
        return result;
    }

    /**
     * Avoid creating a duplicate when deserializing.
     */
    private Object readResolve() {
        if (ok) {
            return MOVE_OK;
        }
        return this;
    }

    /**
     * @return
     */
    public boolean isOk() {
        return ok;
    }

    @Override
    public String toString() {
        return message;
    }
}