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

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Stores the result of a request to log onto the server.
 *
 * @param id assigned player identifier, or {@code -1} for failure
 * @param message failure message, or {@code null} when not supplied
 */
public record LogOnResponse(int id, String message) implements Serializable {

    @Serial
    private static final long serialVersionUID = 3690479099844311344L;

    /**
     * Validates the consistency of the login result.
     *
     * @param id assigned player identifier, or {@code -1} when the login failed
     * @param message failure message, or {@code null} when the login succeeded
     */
    public LogOnResponse {
        if (id >= 0 && message != null) {
            throw new IllegalArgumentException("A successful login response cannot include a failure message.");
        }
        if (id < 0 && message == null) {
            throw new IllegalArgumentException("A failed login response must include a failure message.");
        }
    }

    /**
     * Returns whether the login succeeded.
     *
     * @return {@code true} if a player identifier was assigned
     */
    public boolean success() {
        return id >= 0;
    }

    /**
     * Creates a successful response for an assigned player identifier.
     *
     * @param id assigned player identifier
     * @return a successful login response
     */
    public static LogOnResponse success(int id) {
        return new LogOnResponse(id, null);
    }

    /**
     * Creates a failed response with a message.
     *
     * @param message failure message
     * @return a failed login response
     */
    public static LogOnResponse failure(String message) {
        return new LogOnResponse(-1, Objects.requireNonNull(message, "message"));
    }
}