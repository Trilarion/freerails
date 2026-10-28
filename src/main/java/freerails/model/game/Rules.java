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

    /**
     * Creates a rule set for track connections and network continuity.
     *
     * @param canConnectToOtherPlayersTracks whether track may connect to
     *        another player's track
     * @param mustStayConnectedToExistingTrack whether newly built track must
     *        remain connected to existing track
     */
    public Rules(boolean canConnectToOtherPlayersTracks, boolean mustStayConnectedToExistingTrack) {
        this.canConnectToOtherPlayersTracks = canConnectToOtherPlayersTracks;
        this.mustStayConnectedToExistingTrack = mustStayConnectedToExistingTrack;
    }

    /** Returns a hash code derived from the two rule flags. */
    @Override
    public int hashCode() {
        int result;
        result = canConnectToOtherPlayersTracks ? 1 : 0;
        result = 29 * result + (mustStayConnectedToExistingTrack ? 1 : 0);

        return result;
    }

    /**
     * @return {@code true} if connecting to another player's track is allowed
     */
    public boolean canConnectToOtherPlayersTracks() {
        return canConnectToOtherPlayersTracks;
    }

    /**
     * @return {@code true} if track must remain connected to existing track
     */
    public boolean mustStayConnectedToExistingTrack() {
        return mustStayConnectedToExistingTrack;
    }

    /**
     * Compares both rule flags with those of another rule set.
     *
     * @param obj object to compare
     * @return {@code true} if {@code obj} is a rule set with identical flags
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Rules)) {
            return false;
        }

        Rules test = (Rules) obj;

        return canConnectToOtherPlayersTracks == test.canConnectToOtherPlayersTracks && mustStayConnectedToExistingTrack == test.mustStayConnectedToExistingTrack;
    }
}