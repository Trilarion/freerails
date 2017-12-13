/*
 * Copyright (C) 2002 Luke Lindsay
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

/*
 * NullTrackType.java
 *
 * Created on 23 January 2002, 23:13
 */
package jfreerails.world.track;

import java.io.ObjectStreamException;
import java.util.Iterator;
import jfreerails.world.common.OneTileMoveVector;


/**
 *
 * @author  lindsal
 */
final public class NullTrackType implements jfreerails.world.track.TrackRule {
    public static final int NULL_TRACK_TYPE_RULE_NUMBER = -999;
    private static final NullTrackType nullTrackType = new NullTrackType();

    /** Creates new NullTrackType */
    private NullTrackType() {
    }

    private Object readResolve() throws ObjectStreamException {
        return nullTrackType;
    }

    public static NullTrackType getInstance() {
        return nullTrackType;
    }

    public boolean canBuildOnThisTerrainType(String TerrainType) {
        return true; //No track is possible anywhere.
    }

    public jfreerails.world.common.OneTileMoveVector[] getLegalRoutes(
        jfreerails.world.common.OneTileMoveVector directionComingFrom) {
        return new OneTileMoveVector[0];
    }

    public int getMaximumConsecutivePieces() {
        return -1;
    }

    public int getRuleNumber() {
        return NULL_TRACK_TYPE_RULE_NUMBER;
    }

    public String getTypeName() {
        return "NullTrackType";
    }

    public boolean testTrackPieceLegality(int trackTemplateToTest) {
        if (trackTemplateToTest != 0) {
            return false;
        } else {
            return true;
        }
    }

    public boolean trackPieceIsLegal(TrackConfiguration config) {
        return testTrackPieceLegality(config.getTrackGraphicsNumber());
    }

    public Iterator getLegalConfigurationsIterator() {
        throw new UnsupportedOperationException("Method not implemented yet!");
    }

    public TrackPiece getTrackPiece(TrackConfiguration config) {
        throw new UnsupportedOperationException("Method not implemented yet!");
    }

    public boolean isStation() {
        return false;
    }

    public boolean equals(Object o) {
        return o == this;
    }

    public int getStationRadius() {
        return 0;
    }

    public long getPrice() {
        return 0;
    }

    public long getAssetValue() {
	return 0;
    }

    public long getMaintenanceCost() {
        return 0;
    }
}
