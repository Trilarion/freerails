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

/*
 *
 */
package freerails.network;

import java.io.Serializable;

/**
 * Used by the server to store a player's username and password.
 */
public class NameAndPassword implements Serializable {
    
    private static final long serialVersionUID = 3258409551740155956L;

    public final String password;
    public final String username;

    /**
     * @param username
     * @param password
     */
    public NameAndPassword(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NameAndPassword)) return false;
        NameAndPassword test = (NameAndPassword) obj;
        return test.password.equals(password) && test.username.equals(username);
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = result * 37 + password.hashCode();
        result = result * 37 + username.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return username;
    }
}
