/*
 * NullTrackType.java
 *
 * Created on 23 January 2002, 23:13
 */
package jfreerails.world.track;

import java.io.ObjectStreamException;
import java.util.Iterator;
import jfreerails.world.common.Money;
import jfreerails.world.common.OneTileMoveVector;


/**
 * The type of a Null track piece.
 * TODO maybe it would be simplier to get rid of this and jsut check against null!
 * @author  lindsal
 */
final public class NullTrackType implements jfreerails.world.track.TrackRule {
    public static final int NULL_TRACK_TYPE_RULE_NUMBER = -999;
    private static final NullTrackType nullTrackType = new NullTrackType();

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

    public OneTileMoveVector[] getLegalRoutes(
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

    public TrackPiece getTrackPiece(TrackConfiguration config, int owner) {
        throw new UnsupportedOperationException("Method not implemented yet!");
    }

    public boolean isStation() {
        return false;
    }

    public boolean equals(Object o) {
        return o == this;
    }

    public int hashCode() {
        return 666;
    }

    public int getStationRadius() {
        return 0;
    }

    public Money getPrice() {
        return new Money(0);
    }

    public Money getMaintenanceCost() {
        return new Money(0);
    }
}