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
 * Stores the result of a request to log onto the server.
 */
public class LogOnResponse implements Serializable {

    private static final long serialVersionUID = 3690479099844311344L;
    private final boolean successful;
    private final int playerNumber;
    private final String message;

    public LogOnResponse(boolean success, int i) {
        assert(success);
        successful = success;
        playerNumber = i;
        message = null;
    }

    public LogOnResponse(boolean success, String s) {
        assert(!success);
        successful = success;
        playerNumber = -1;
        message = s;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof LogOnResponse)) return false;

        final LogOnResponse logOnResponse = (LogOnResponse) obj;

        if (playerNumber != logOnResponse.playerNumber) return false;
        if (successful != logOnResponse.successful) return false;
        return message != null ? message.equals(logOnResponse.message) : logOnResponse.message == null;
    }

    @Override
    public int hashCode() {
        int result;
        result = (successful ? 1 : 0);
        result = 29 * result + playerNumber;
        result = 29 * result + (message != null ? message.hashCode() : 0);
        return result;
    }

    /**
     * @return
     */
    public int getPlayerID() {
        return playerNumber;
    }

    /**
     * @return
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return
     */
    public boolean isSuccessful() {
        return successful;
    }
}