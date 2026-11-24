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

import java.io.Serial;
import java.io.Serializable;
import java.util.Optional;

/**
 * Immutable description of the track configuration, type, and owner on a tile.

 * @param trackConfiguration the configuration of the track piece
 * @param trackType the type of track piece
 * @param playerId the identifier of the player who owns the track piece
 */
public record TrackPiece(TrackConfiguration trackConfiguration, TrackType trackType, int playerId) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * @param trackConfiguration geometric configuration of the track
     * @param trackType          type and properties of the track
     * @param playerId           ID of the owning player
     */
    public TrackPiece {
    }

    /**
     * Creates a new track piece containing only the supplied connection.
     *
     * @param direction the connection of the new piece
     * @param trackType the type of the new piece
     * @param owner the identifier of the owning player
     * @return the new track piece
     */
    public static TrackPiece create(TrackConfigurations direction, TrackType trackType, int owner) {
        return new TrackPiece(TrackConfiguration.getFlatInstance(direction), trackType, owner);
    }

    /**
     * Returns this piece with the supplied connection added; the track type is kept.
     *
     * @param direction the connection to add
     * @param owner the identifier of the owning player of the result
     * @return the changed track piece
     */
    public TrackPiece withConnection(TrackConfigurations direction, int owner) {
        return new TrackPiece(TrackConfiguration.add(trackConfiguration, direction), trackType, owner);
    }

    /**
     * Returns this piece with the supplied connection removed; the track type is kept.
     *
     * @param direction the connection to remove
     * @param owner the identifier of the owning player of the result
     * @return the changed track piece, or empty if no connection remains
     */
    public Optional<TrackPiece> withoutConnection(TrackConfigurations direction, int owner) {
        TrackConfiguration remaining = TrackConfiguration.subtract(trackConfiguration, direction);
        if (remaining.get8bitTemplate() == 0) {
            return Optional.empty();
        }
        return Optional.of(new TrackPiece(remaining, trackType, owner));
    }

    /**
     * Returns this piece with another track type; the configuration is kept.
     *
     * @param newTrackType the track type of the result
     * @param owner the identifier of the owning player of the result
     * @return the changed track piece
     */
    public TrackPiece withTrackType(TrackType newTrackType, int owner) {
        return new TrackPiece(trackConfiguration, newTrackType, owner);
    }

    /**
     * Returns the encoded track configuration used by track graphics.
     *
     * @return track graphic/configuration ID
     */
    public int getTrackGraphicID() {
        return trackConfiguration.getConfiguration();
    }

}