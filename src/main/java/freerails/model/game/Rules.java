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

import freerails.model.track.TrackPiece;
import freerails.model.track.TrackUtils;

import java.io.Serial;
import java.io.Serializable;

/**
 * Stores rules governing what players are allowed to do, including track
 * connection, network continuity, and track-removal permissions.

 * @param canConnectToOtherPlayersTracks whether players may connect track to
 *                                        another player's track
 * @param mustStayConnectedToExistingTrack whether new track must remain
 *                                         connected to existing track
 * @param canRemoveOtherPlayersTracks whether players may remove another
 *                                     player's track
 */
public record Rules(
        boolean canConnectToOtherPlayersTracks,
        boolean mustStayConnectedToExistingTrack,
        boolean canRemoveOtherPlayersTracks) implements Serializable {

    @Serial
    private static final long serialVersionUID = 3258125847557978416L;

    /**
     * Creates a rule set for track connections, network continuity, and
     * track-removal permission.
     *
     * @param canConnectToOtherPlayersTracks   whether track may connect to
     *                                         another player's track
     * @param mustStayConnectedToExistingTrack whether newly built track must
     *                                         remain connected to the player's
     *                                         own existing track
     */
    public Rules {
    }

    /**
     * Creates rules with track removal permission matching track connection
     * permission.
     *
     * @param canConnectToOtherPlayersTracks whether track may connect to
     *                                       another player's track
     * @param mustStayConnectedToExistingTrack whether newly built track must
     *                                         remain connected to the player's
     *                                         own existing track
     */
    public Rules(boolean canConnectToOtherPlayersTracks, boolean mustStayConnectedToExistingTrack) {
        this(canConnectToOtherPlayersTracks, mustStayConnectedToExistingTrack, canConnectToOtherPlayersTracks);
    }

    /**
     * Determines whether a player may remove connections from a track piece.
     *
     * @param playerId player attempting the change
     * @param before track piece before the change, or {@code null}
     * @param after track piece after the change, or {@code null}
     * @return {@code true} if the change removes no connections, the piece is
     *         owned by the player, or the rules permit removing another
     *         player's track
     */
    public boolean canRemoveTrackPiece(int playerId, TrackPiece before, TrackPiece after) {
        if (before == null || before.playerId() == playerId) {
            return true;
        }

        return !TrackUtils.removesTrackConnections(before, after) || canRemoveOtherPlayersTracks;
    }

}