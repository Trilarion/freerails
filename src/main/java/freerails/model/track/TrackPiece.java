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

package freerails.model.track;

import java.io.Serializable;

/**
 * Defines methods to access the properties of the track on a tile.
 * Represents the track on a tile.
 */
public class TrackPiece implements Serializable {

    private static final long serialVersionUID = 4049080423458027569L;
    private final TrackConfiguration trackConfiguration;
    private final TrackType trackType;
    private final int playerId;

    /**
     * @param trackConfiguration
     * @param playerId
     */
    public TrackPiece(TrackConfiguration trackConfiguration, TrackType trackType, int playerId) {
        this.trackConfiguration = trackConfiguration;
        this.trackType = trackType;
        this.playerId = playerId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        final TrackPiece other = (TrackPiece) obj;

        if (playerId != other.playerId) return false;
        if (!trackConfiguration.equals(other.trackConfiguration)) return false;
        return trackType.equals(other.trackType);
    }

    @Override
    public int hashCode() {
        int result;
        result = trackConfiguration.hashCode();
        result = 29 * result + trackType.hashCode();
        result = 29 * result + playerId;
        return result;
    }

    /**
     * @return
     */
    public int getTrackGraphicID() {
        return trackConfiguration.getConfiguration();
    }

    /**
     * @return
     */
    public TrackConfiguration getTrackConfiguration() {
        return trackConfiguration;
    }

    /**
     * @return
     */
    public int getPlayerId() {
        return playerId;
    }

    public TrackType getTrackType() {
        return trackType;
    }
}