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

package freerails.network;

import java.io.Serializable;
import java.util.Objects;

/**
 * Stores the result of a request to log onto the server.
 */
public record LogOnResponse(boolean success, int id, String message) implements Serializable {

    private static final long serialVersionUID = 3690479099844311344L;

    public LogOnResponse(boolean success, int id) {
        this(success, id, null);
        assert success;
    }

    // TODO the usage of success is strange, why here always false
    public LogOnResponse(boolean success, String s) {
        this(success, -1, s);
        assert !success;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof LogOnResponse logOnResponse)) return false;

        if (id != logOnResponse.id) return false;
        if (success != logOnResponse.success) return false;
        return Objects.equals(message, logOnResponse.message);
    }

    @Override
    public int hashCode() {
        int result;
        result = Boolean.hashCode(success);
        result = 29 * result + id;
        result = 29 * result + (message != null ? message.hashCode() : 0);
        return result;
    }

    /**
     * @return
     */
    @Override
    public int id() {
        return id;
    }

    /**
     * @return
     */
    @Override
    public String message() {
        return message;
    }

    /**
     * @return
     */
    @Override
    public boolean success() {
        return success;
    }
}