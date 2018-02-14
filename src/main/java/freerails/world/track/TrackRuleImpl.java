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

import freerails.world.finances.Money;
import freerails.world.terrain.TerrainCategory;

import java.util.Iterator;

/**
 * Encapsulates the rules that apply to a type of track node. They
 * concern: the legal routes trains can travel across the node, whether the
 * node's track can be doubled, on which terrain types it can be built, and the
 * maximum number of consecutive nodes of this type (used for bridges and
 * tunnels).
 */
public final class TrackRuleImpl implements TrackRule {

    private static final long serialVersionUID = 3257281414171801401L;
    private final ValidTrackConfigurations validTrackConfigurations;
    private final ValidTrackPlacement validTrackPlacement;
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

    public TrackRuleImpl(TrackRuleProperties p, ValidTrackConfigurations lc, ValidTrackPlacement ltp) {
        if (null == p || null == lc || null == ltp) {
            throw new java.lang.IllegalArgumentException();
        }
        properties = p;
        validTrackConfigurations = lc;
        validTrackPlacement = ltp;
    }

    /**
     * @param TerrainType
     * @return
     */
    public boolean canBuildOnThisTerrainType(TerrainCategory TerrainType) {
        return validTrackPlacement.canBuildOnThisTerrain(TerrainType);
    }

    /**
     * If the specified object is a track rule, comparison is by category then price.
     */
    public int compareTo(TrackRule o) {

        int comp = o.getCategory().compareTo(getCategory());
        if (comp != 0) {
            return -comp;
        }
        return properties.getPrice().compareTo(o.getPrice());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TrackRuleImpl) {
            TrackRuleImpl trackRuleImpl = (TrackRuleImpl) obj;
            boolean propertiesFieldsEqual = properties.equals(trackRuleImpl.properties);
            boolean legalConfigurationsEqual = validTrackConfigurations.equals(trackRuleImpl.validTrackConfigurations);
            boolean legalTrackPlacementEqual = validTrackPlacement.equals(trackRuleImpl.validTrackPlacement);

            return propertiesFieldsEqual && legalConfigurationsEqual && legalTrackPlacementEqual;
        }
        return false;
    }

    /**
     * @return
     */
    public TrackCategories getCategory() {
        return properties.getCategory();
    }

    /**
     * @return
     */
    public Iterator<TrackConfiguration> getLegalConfigurationsIterator() {
        return validTrackConfigurations.getLegalConfigurationsIterator();
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
    public Money getPrice() {
        return properties.getPrice();
    }

    /**
     * @return
     */
    public int getStationRadius() {
        return properties.getStationRadius();
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
        result = 29 * result + validTrackConfigurations.hashCode();
        result = 29 * result + validTrackPlacement.hashCode();

        return result;
    }

    /**
     * @return
     */
    public boolean isStation() {
        return properties.isStation();
    }

    /**
     * @param a9bitTemplate
     * @return
     */
    public boolean testTrackPieceLegality(int a9bitTemplate) {
        TrackConfiguration trackConfiguration = TrackConfiguration.from9bitTemplate(a9bitTemplate);

        return validTrackConfigurations.trackConfigurationIsLegal(trackConfiguration);
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
        return validTrackConfigurations.trackConfigurationIsLegal(config);
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