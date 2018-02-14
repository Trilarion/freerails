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
 */
public class TrackPieceImpl implements TrackPiece {

    private static final long serialVersionUID = 4049080423458027569L;
    private final TrackConfiguration trackConfiguration;
    private final TrackRule trackRule;
    private final int ownerID;
    private final int ruleNumber;

    /**
     * @param trackConfiguration
     * @param trackRule
     * @param owner
     * @param rule
     */
    // TODO is rule number really needed?
    public TrackPieceImpl(TrackConfiguration trackConfiguration, TrackRule trackRule, int owner, int rule) {
        this.trackConfiguration = trackConfiguration;
        this.trackRule = trackRule;
        ownerID = owner;
        ruleNumber = rule;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        final TrackPieceImpl that = (TrackPieceImpl) obj;

        if (ownerID != that.ownerID) return false;
        if (ruleNumber != that.ruleNumber) return false;
        if (!trackConfiguration.equals(that.trackConfiguration)) return false;
        return trackRule.equals(that.trackRule);
    }

    @Override
    public int hashCode() {
        int result;
        result = trackConfiguration.hashCode();
        result = 29 * result + trackRule.hashCode();
        result = 29 * result + ownerID;
        result = 29 * result + ruleNumber;
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
    public TrackRule getTrackRule() {
        return trackRule;
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
    public int getOwnerID() {
        return ownerID;
    }

    /**
     * @return
     */
    // TODO abuse of rule number?
    public int getTrackTypeID() {
        return ruleNumber;
    }
}