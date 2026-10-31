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

/**
 * Stores the result of a request to log onto the server.
 *
 * @param success whether the request succeeded
 * @param id assigned player identifier, or {@code -1} for failure
 * @param message failure message, or {@code null} when not supplied
 */
public record LogOnResponse(boolean success, int id, String message) implements Serializable {

    private static final long serialVersionUID = 3690479099844311344L;

    /**
     * Creates a successful response for an assigned player identifier.
     *
     * @param success success flag, expected to be {@code true}
     * @param id assigned player identifier
     */
    public LogOnResponse(boolean success, int id) {
        this(success, id, null);
        assert success;
    }

    // TODO the usage of success is strange, why here always false
    /**
     * Creates a failed response with a message.
     *
     * @param success success flag, expected to be {@code false}
     * @param s failure message
     */
    public LogOnResponse(boolean success, String s) {
        this(success, -1, s);
        assert !success;
    }

}