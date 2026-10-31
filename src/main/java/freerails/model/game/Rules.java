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
 * Stores rules governing what players are allowed to do. Track connections
 * have two independent rules: whether a player may connect to another player's
 * track, and whether the player's new track must stay connected to their own
 * existing track.
 */
public record Rules(boolean canConnectToOtherPlayersTracks, boolean mustStayConnectedToExistingTrack) implements Serializable {

    private static final long serialVersionUID = 3258125847557978416L;

    /**
     * Creates a rule set for track connections and network continuity. The
     * two track rules are independent.
     *
     * @param canConnectToOtherPlayersTracks   whether track may connect to
     *                                         another player's track
     * @param mustStayConnectedToExistingTrack whether newly built track must
     *                                         remain connected to the player's
     *                                         own existing track
     */
    public Rules {
    }

}