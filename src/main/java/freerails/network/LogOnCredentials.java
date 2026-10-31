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

/*
 *
 */
package freerails.network;

import freerails.util.Utils;

import java.io.Serializable;

/**
 * Used by the server to store a player's username and password.
 *
 * @param username account name
 * @param password account password
 */
public record LogOnCredentials(String username, String password) implements Serializable {

    private static final long serialVersionUID = 3258409551740155956L;

    /**
     * Creates credentials with non-null username and password values.
     *
     * @param username account name
     * @param password account password
     * @throws NullPointerException if either value is null
     */
    public LogOnCredentials(String username, String password) {
        this.username = Utils.verifyNotNull(username);
        this.password = Utils.verifyNotNull(password);
    }

    /**
     * Returns the username without including the password.
     *
     * @return the username
     */
    @Override
    public String toString() {
        return username;
    }
}
