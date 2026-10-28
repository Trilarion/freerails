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

package freerails.model.track;

import java.io.Serializable;

/**
 * Immutable description of the track configuration, type, and owner on a tile.
 */
public record TrackPiece(TrackConfiguration trackConfiguration, TrackType trackType, int playerId) implements Serializable {

    private static final long serialVersionUID = 4049080423458027569L;

    /**
     * @param trackConfiguration geometric configuration of the track
     * @param trackType          type and properties of the track
     * @param playerId           ID of the owning player
     */
    public TrackPiece {
    }

    /**
     * Tests equality across the configuration, type, and owner.
     *
     * @param obj object to compare
     * @return {@code true} when all track-piece properties match
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        final TrackPiece other = (TrackPiece) obj;

        if (playerId != other.playerId) return false;
        if (!trackConfiguration.equals(other.trackConfiguration)) return false;
        return trackType.equals(other.trackType);
    }

    /**
     * Returns the encoded track configuration used by track graphics.
     *
     * @return track graphic/configuration ID
     */
    public int getTrackGraphicID() {
        return trackConfiguration.getConfiguration();
    }

    /**
     * Returns the directional configuration of this piece.
     *
     * @return track configuration
     */
    @Override
    public TrackConfiguration trackConfiguration() {
        return trackConfiguration;
    }

    /**
     * Returns the ID of the player who owns this piece.
     *
     * @return owner player ID
     */
    @Override
    public int playerId() {
        return playerId;
    }

    /**
     * Returns the type of this track piece.
     *
     * @return track type
     */
    @Override
    public TrackType trackType() {
        return trackType;
    }
}