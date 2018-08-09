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

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Records the status or failure of, among other things, an attempt to execute a move.
 */
public class Status implements Serializable {

    /**
     *
     */
    public static final Status OK = new Status(true, null);
    private static final long serialVersionUID = 3258129171879309624L;

    private final boolean status;
    private final String message;

    private Status(boolean status, String message) {
        this.status = status;
        this.message = message;
    }

    /**
     * @param reason
     * @return
     */
    public static Status moveFailed(@NotNull String reason) {
        return new Status(false, reason);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Status)) return false;

        final Status status = (Status) obj;

        if (this.status != status.status) return false;
        return message != null ? message.equals(status.message) : status.message == null;
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
            return OK;
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