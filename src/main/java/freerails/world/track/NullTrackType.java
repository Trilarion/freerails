/*
 * NullTrackType.java
 *
 * Created on 23 January 2002, 23:13
 */
package freerails.world.track;

import java.io.ObjectStreamException;
import java.util.Iterator;

import freerails.world.common.Money;
import freerails.world.common.Step;
import freerails.world.terrain.TerrainType;

/**
 * The type of a Null track piece. TODO maybe it would be simplier to get rid of
 * this and jsut check against null!
 * 
 * @author lindsal
 */
final public class NullTrackType implements TrackRule {
    private static final long serialVersionUID = 3257849891614306614L;

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

    public boolean canBuildOnThisTerrainType(TerrainType.Category TerrainType) {
        return true; // No track is possible anywhere.
    }

    public Step[] getLegalRoutes(
            freerails.world.common.Step directionComingFrom) {
        return new Step[0];
    }

    public int getMaximumConsecutivePieces() {
        return -1;
    }

    public String getTypeName() {
        return "NullTrackType";
    }

    public boolean testTrackPieceLegality(int trackTemplateToTest) {
        if (trackTemplateToTest != 0) {
            return false;
        }
        return true;
    }

    public boolean trackPieceIsLegal(TrackConfiguration config) {
        return testTrackPieceLegality(config.getTrackGraphicsID());
    }

    public Iterator<TrackConfiguration> getLegalConfigurationsIterator() {
        throw new UnsupportedOperationException("Method not implemented yet!");
    }

    public TrackPiece getTrackPiece(TrackConfiguration config, int owner) {
        throw new UnsupportedOperationException("Method not implemented yet!");
    }

    public boolean isStation() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        return o == this;
    }

    @Override
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

    public TrackCategories getCategory() {
        return TrackCategories.non;
    }

    public int compareTo(TrackRule arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    public boolean isDouble() {

        return false;
    }

    public Money getFixedCost() {
        return Money.ZERO;
    }
}