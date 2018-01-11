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

/*
 * NullTrackPiece.java
 *
 */
package freerails.world.track;

// TODO do we really need that? use NULL instead?
/**
 * A track piece that doesn't exist - using this avoids needing to check against
 * null before calling the methods on a track piece.
 */
public final class NullTrackPiece implements TrackPiece {

    private static final long serialVersionUID = 3258413915376268599L;
    private static final TrackPiece nullTrackPiece = new NullTrackPiece();
    private static final int NO_OWNER = Integer.MIN_VALUE;

    private NullTrackPiece() {
    }

    /**
     * @return
     */
    public static TrackPiece getInstance() {
        return nullTrackPiece;
    }

    /**
     * @return
     */
    public int getTrackGraphicID() {
        return 0;
    }

    /**
     * @return
     */
    public TrackRule getTrackRule() {
        return NullTrackType.getInstance();
    }

    /**
     * @return
     */
    public TrackConfiguration getTrackConfiguration() {
        return TrackConfiguration.from9bitTemplate(0);
    }

    private Object readResolve() {
        return nullTrackPiece;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }

    @Override
    public int hashCode() {
        return 777;
    }

    /**
     * @return
     */
    public int getOwnerID() {
        return NO_OWNER;
    }

    /**
     * @return
     */
    public int getTrackTypeID() {
        return NullTrackType.NULL_TRACK_TYPE_RULE_NUMBER;
    }
}