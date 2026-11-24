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
import java.util.Optional;

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
    private static final long serialVersionUID = 1L;

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
     * Checks whether a player may change the track piece on one tile, as far as
     * track ownership is concerned.
     *
     * <ul>
     *     <li>New track belongs to the player who builds it.</li>
     *     <li>The owner of existing track never changes.</li>
     *     <li>A player may change their own track.</li>
     *     <li>On another player's track, a player may only add connections if
     *     {@link #canConnectToOtherPlayersTracks()}, and only remove connections
     *     or the whole piece if {@link #canRemoveOtherPlayersTracks()}. Changing
     *     its track type, or adding and removing connections at once, is never
     *     allowed.</li>
     * </ul>
     *
     * @param playerId the player making the change
     * @param before the track piece before the change, or {@code null}
     * @param after the track piece after the change, or {@code null}
     * @return the reason the change is not allowed, or empty if it is allowed
     */
    public Optional<String> checkTrackChange(int playerId, TrackPiece before, TrackPiece after) {
        if (before == null) {
            if (after != null && after.playerId() != playerId) {
                return Optional.of("New track must belong to the player who builds it.");
            }
            return Optional.empty();
        }
        if (after != null && after.playerId() != before.playerId()) {
            return Optional.of("The owner of track cannot change.");
        }
        if (before.playerId() == playerId) {
            return Optional.empty();
        }

        // Another player's track.
        if (after == null) {
            return canRemoveOtherPlayersTracks
                    ? Optional.empty()
                    : Optional.of("Not allowed to remove another player's track.");
        }
        if (!before.trackType().equals(after.trackType())) {
            return Optional.of("Not allowed to change the track type of another player's track.");
        }
        boolean adds = TrackUtils.addsTrackConnections(before, after);
        boolean removes = TrackUtils.removesTrackConnections(before, after);
        if (adds && removes) {
            return Optional.of("Not allowed to rebuild another player's track.");
        }
        if (adds && !canConnectToOtherPlayersTracks) {
            return Optional.of("Not allowed to connect to another player's track.");
        }
        if (removes && !canRemoveOtherPlayersTracks) {
            return Optional.of("Not allowed to remove another player's track.");
        }
        return Optional.empty();
    }

}