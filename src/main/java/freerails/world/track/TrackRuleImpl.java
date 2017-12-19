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
 *  TrackRule.java
 *
 *  Created on 15 July 2001, 19:53
 */
package freerails.world.track;

import freerails.world.TileTransition;
import freerails.world.finances.Money;
import freerails.world.terrain.TerrainCategory;

import java.util.Iterator;

/**
 * This class encapsulates the rules that apply to a type of track node. They
 * concern: the legal routes trains can travel across the node, whether the
 * node's track can be doubled, on which terrain types it can be built, and the
 * maximum number of consecutive nodes of this type (used for bridges and
 * tunnels).
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

    /**
     * @param p
     * @param lc
     * @param ltp
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

    /**
     * @param TerrainType
     * @return
     */
    public boolean canBuildOnThisTerrainType(TerrainCategory TerrainType) {
        return legalTrackPlacement.canBuildOnThisTerrain(TerrainType);
    }

    /**
     * If the specified object is a track rule, comparison is by category then
     * price.
     *
     * @param otherRule
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

            return propertiesFieldsEqual && legalConfigurationsEqual
                    && legalTrackPlacementEqual;
        }
        return false;
    }

    /**
     * @return
     */
    public TrackRule.TrackCategories getCategory() {
        return properties.getCategory();
    }

    /**
     * @return
     */
    public LegalTrackConfigurations getLegalConfigurations() {
        return legalConfigurations;
    }

    /**
     * @return
     */
    public Iterator<TrackConfiguration> getLegalConfigurationsIterator() {
        return legalConfigurations.getLegalConfigurationsIterator();
    }

    /**
     * @param directionComingFrom
     * @return
     */
    public TileTransition[] getLegalRoutes(TileTransition directionComingFrom) {
        // TODO add code..
        return null;
    }

    /**
     * @return
     */
    public LegalTrackPlacement getLegalTrackPlacement() {
        return legalTrackPlacement;
    }

    /**
     * @return
     */
    public Money getMaintenanceCost() {
        return properties.getMaintenanceCost();
    }

    /**
     * @return
     */
    public int getMaximumConsecutivePieces() {
        return legalConfigurations.getMaximumConsecutivePieces();
    }

    /**
     * @return
     */
    public Money getPrice() {
        return this.properties.getPrice();
    }

    /**
     * @return
     */
    public TrackRuleProperties getProperties() {
        return properties;
    }

    /**
     * @return
     */
    public int getStationRadius() {
        return this.properties.getStationRadius();
    }

    /**
     * @return
     */
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

    /**
     * @return
     */
    public boolean isStation() {
        return properties.isStation();
    }

    /**
     * @param trackTemplateToTest
     * @return
     */
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

    /**
     * @param config
     * @return
     */
    public boolean trackPieceIsLegal(TrackConfiguration config) {
        return legalConfigurations.trackConfigurationIsLegal(config);
    }

    /**
     * @return
     */
    public boolean isDouble() {

        return properties.isEnableDoubleTrack();
    }

    /**
     * @return
     */
    public Money getFixedCost() {
        return properties.getFixedCost();
    }
}