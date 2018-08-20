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

package freerails.nove;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

// TODO does it really have to be serializable?
/**
 * Records the status or failure of, among other things, an attempt to execute a move.
 */
public class Status implements Serializable {

    public static final Status OK = new Status(true, null);
    private final boolean success;
    private final String message;

    private Status(boolean success, String message) {
        if (!success && message == null) {
            throw new IllegalArgumentException();
        }
        this.success = success;
        this.message = message;
    }

    public static Status fail(@NotNull String message) {
        return new Status(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Status)) return false;

        final Status other = (Status) obj;

        if (this.success != other.success) return false;
        return Objects.equals(this.message, other.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, message);
    }

    @Override
    public String toString() {
        if (success) {
            return "OK";
        } else {
            return "Failed: " + message;
        }
    }
}
