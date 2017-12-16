/*
 *  TrackRule.java
 *
 *  Created on 15 July 2001, 19:53
 */
package freerails.world.track;

import freerails.world.common.Money;
import freerails.world.common.Step;
import freerails.world.terrain.TerrainType;

import java.util.Iterator;

/**
 * This class encapsulates the rules that apply to a type of track node. They
 * concern: the legal routes trains can travel across the node, whether the
 * node's track can be doubled, on which terrain types it can be built, and the
 * maximum number of consecutive nodes of this type (used for bridges and
 * tunnels).
 *
 * @author Luke Lindsay 09 October 2001
 */
final public class TrackRuleImpl implements TrackRule {
    private static final long serialVersionUID = 3257281414171801401L;

    private final LegalTrackConfigurations legalConfigurations;

    private final LegalTrackPlacement legalTrackPlacement;

    private final TrackRuleProperties properties;

    /*
     * Track templates are 9 bit values, so there are 512 possible templates. If
     * legalTrackTemplate[x]==true, then x is a legal track-template. Example:
     * 000 111 000 This represents a horizontal straight.
     */
    public TrackRuleImpl(TrackRuleProperties p, LegalTrackConfigurations lc,
                         LegalTrackPlacement ltp) {
        if (null == p || null == lc || null == ltp) {
            throw new java.lang.IllegalArgumentException();
        }
        properties = p;
        legalConfigurations = lc;
        legalTrackPlacement = ltp;
    }

    public boolean canBuildOnThisTerrainType(TerrainType.Category TerrainType) {
        return legalTrackPlacement.canBuildOnThisTerrain(TerrainType);
    }

    /**
     * If the specified object is a track rule, comparison is by category then
     * price.
     */
    public int compareTo(TrackRule otherRule) {

        int comp = otherRule.getCategory().compareTo(getCategory());
        if (comp != 0) {
            return -comp;
        }
        long dPrice = this.properties.getPrice().getAmount()
                - otherRule.getPrice().getAmount();
        return (int) dPrice;

    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof TrackRuleImpl) {
            TrackRuleImpl trackRuleImpl = (TrackRuleImpl) o;
            boolean propertiesFieldsEqual = this.properties
                    .equals(trackRuleImpl.getProperties());
            boolean legalConfigurationsEqual = this.legalConfigurations
                    .equals(trackRuleImpl.getLegalConfigurations());
            boolean legalTrackPlacementEqual = this.legalTrackPlacement
                    .equals(trackRuleImpl.getLegalTrackPlacement());

            if (propertiesFieldsEqual && legalConfigurationsEqual
                    && legalTrackPlacementEqual) {
                return true;
            }
            return false;
        }
        return false;
    }

    public TrackRule.TrackCategories getCategory() {
        return properties.getCategory();
    }

    public LegalTrackConfigurations getLegalConfigurations() {
        return legalConfigurations;
    }

    public Iterator<TrackConfiguration> getLegalConfigurationsIterator() {
        return legalConfigurations.getLegalConfigurationsIterator();
    }

    public Step[] getLegalRoutes(Step directionComingFrom) {
        // TODO add code..
        return null;
    }

    public LegalTrackPlacement getLegalTrackPlacement() {
        return legalTrackPlacement;
    }

    public Money getMaintenanceCost() {
        return properties.getMaintenanceCost();
    }

    public int getMaximumConsecutivePieces() {
        return legalConfigurations.getMaximumConsecutivePieces();
    }

    public Money getPrice() {
        return this.properties.getPrice();
    }

    public TrackRuleProperties getProperties() {
        return properties;
    }

    public int getStationRadius() {
        return this.properties.getStationRadius();
    }

    public String getTypeName() {
        return properties.getTypeName();
    }

    @Override
    public int hashCode() {
        int result;
        result = properties.hashCode();
        result = 29 * result + legalConfigurations.hashCode();
        result = 29 * result + legalTrackPlacement.hashCode();

        return result;
    }

    public boolean isStation() {
        return properties.isStation();
    }

    public boolean testTrackPieceLegality(int trackTemplateToTest) {
        TrackConfiguration trackConfiguration = TrackConfiguration
                .from9bitTemplate(trackTemplateToTest);

        return legalConfigurations
                .trackConfigurationIsLegal(trackConfiguration);
    }

    @Override
    public String toString() {
        return getTypeName();
    }

    public boolean trackPieceIsLegal(TrackConfiguration config) {
        return legalConfigurations.trackConfigurationIsLegal(config);
    }

    public boolean isDouble() {

        return properties.isEnableDoubleTrack();
    }

    public Money getFixedCost() {
        return properties.getFixedCost();
    }
}