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

package freerails.world.track;

/**
 * Represents the track on a tile.
 *
 */
final public class TrackPieceImpl implements TrackPiece {
    private static final long serialVersionUID = 4049080423458027569L;

    private final TrackConfiguration configuration;

    private final TrackRule trackType;

    private final int ownerID;

    private final int ruleNumber;

    /**
     *
     * @param c
     * @param type
     * @param owner
     * @param rule
     */
    public TrackPieceImpl(TrackConfiguration c, TrackRule type, int owner,
                          int rule) {
        configuration = c;
        trackType = type;
        ownerID = owner;
        ruleNumber = rule;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final TrackPieceImpl that = (TrackPieceImpl) o;

        if (ownerID != that.ownerID)
            return false;
        if (ruleNumber != that.ruleNumber)
            return false;
        if (!configuration.equals(that.configuration))
            return false;
        return trackType.equals(that.trackType);
    }

    @Override
    public int hashCode() {
        int result;
        result = configuration.hashCode();
        result = 29 * result + trackType.hashCode();
        result = 29 * result + ownerID;
        result = 29 * result + ruleNumber;
        return result;
    }

    /**
     *
     * @return
     */
    public int getTrackGraphicID() {
        return configuration.getTrackGraphicsID();
    }

    /**
     *
     * @return
     */
    public TrackRule getTrackRule() {
        return trackType;
    }

    /**
     *
     * @return
     */
    public TrackConfiguration getTrackConfiguration() {
        return configuration;
    }

    /**
     *
     * @return
     */
    public int getOwnerID() {
        return ownerID;
    }

    /**
     *
     * @return
     */
    public int getTrackTypeID() {
        return ruleNumber;
    }
}