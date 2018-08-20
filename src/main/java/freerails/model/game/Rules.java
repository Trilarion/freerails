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

package freerails.model.game;

import java.io.Serializable;

/**
 * Stores rules governing what players are allowed to do, for example whether
 * they can connect their track to the track of other players.
 */
public class Rules implements Serializable {

    private static final long serialVersionUID = 3258125847557978416L;
    private final boolean canConnectToOtherPlayersTracks;
    private final boolean mustStayConnectedToExistingTrack;

    public Rules(boolean canConnectToOtherPlayersTracks, boolean mustStayConnectedToExistingTrack) {
        this.canConnectToOtherPlayersTracks = canConnectToOtherPlayersTracks;
        this.mustStayConnectedToExistingTrack = mustStayConnectedToExistingTrack;
    }

    @Override
    public int hashCode() {
        int result;
        result = (canConnectToOtherPlayersTracks ? 1 : 0);
        result = 29 * result + (mustStayConnectedToExistingTrack ? 1 : 0);

        return result;
    }

    /**
     * @return
     */
    public boolean canConnectToOtherPlayersTracks() {
        return canConnectToOtherPlayersTracks;
    }

    /**
     * @return
     */
    public boolean mustStayConnectedToExistingTrack() {
        return mustStayConnectedToExistingTrack;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Rules)) {
            return false;
        }

        Rules test = (Rules) obj;

        return canConnectToOtherPlayersTracks == test.canConnectToOtherPlayersTracks && mustStayConnectedToExistingTrack == test.mustStayConnectedToExistingTrack;
    }
}