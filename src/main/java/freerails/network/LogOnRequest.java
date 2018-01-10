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

package freerails.network;

import java.io.Serializable;

/**
 * A client sends an instance of this class to the server when it wishes to log
 * on.
 */
public class LogOnRequest implements Serializable {

    private static final long serialVersionUID = 3257854263924240949L;
    private final String username;
    private final String password;

    /**
     * @param username
     * @param password
     */
    public LogOnRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof LogOnRequest))
            return false;

        final LogOnRequest logOnRequest = (LogOnRequest) o;

        if (password != null ? !password.equals(logOnRequest.password)
                : logOnRequest.password != null)
            return false;
        return username != null ? username.equals(logOnRequest.username) : logOnRequest.username == null;
    }

    @Override
    public int hashCode() {
        int result;
        result = (username != null ? username.hashCode() : 0);
        result = 29 * result + (password != null ? password.hashCode() : 0);
        return result;
    }

    /**
     * @return
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return
     */
    public String getUsername() {
        return username;
    }
}