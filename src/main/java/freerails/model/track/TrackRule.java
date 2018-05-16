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
package freerails.model.track;

import freerails.model.finances.Money;
import freerails.model.terrain.TerrainCategory;

import java.io.Serializable;
import java.util.Iterator;

/**
 * Defines methods to access the properties of a track type.
 *
 * Encapsulates the rules that apply to a type of track node. They
 * concern: the legal routes trains can travel across the node, whether the
 * node's track can be doubled, on which terrain types it can be built, and the
 * maximum number of consecutive nodes of this type (used for bridges and
 * tunnels).
 */
public class TrackRule implements Serializable, Comparable<TrackRule> {

    private static final long serialVersionUID = 3257281414171801401L;
    private final ValidTrackConfigurations validTrackConfigurations;
    private final ValidTrackPlacement validTrackPlacement;
    private final TrackCategory trackCategory;
    private final String name;
    private final boolean doubleTracked;
    private final int stationRadius;
    private final Money maintenanceCost;
    private final Money price;
    private final Money fixedCost;

    /*
     * Track templates are 9 bit values, so there are 512 possible templates. If
     * legalTrackTemplate[x]==true, then x is a legal track-template. Example:
     * 000 111 000 This represents a horizontal straight.
     */

    /**

     * @param lc
     * @param ltp
     */
    public TrackRule(ValidTrackConfigurations lc, ValidTrackPlacement ltp, TrackCategory trackCategory, String name, boolean doubleTracked, int stationRadius, Money maintenanceCost, Money price, Money fixedCost) {
        if (null == lc || null == ltp) {
            throw new java.lang.IllegalArgumentException();
        }
        validTrackConfigurations = lc;
        validTrackPlacement = ltp;
        this.trackCategory = trackCategory;
        this.name = name;
        this.doubleTracked = doubleTracked;
        this.stationRadius = stationRadius;
        this.maintenanceCost = maintenanceCost;
        this.price = price;
        this.fixedCost = fixedCost;
    }

    /**
     * @param TerrainType
     * @return
     */
    public boolean canBuildOnThisTerrainType(TerrainCategory TerrainType) {
        return validTrackPlacement.canBuildOnThisTerrain(TerrainType);
    }

    // TODO equals and compareTo and possibly hascode are not complete!
    /**
     * If the specified object is a track rule, comparison is by category then price.
     */
    public int compareTo(TrackRule o) {

        int comp = o.getCategory().compareTo(trackCategory);
        if (comp != 0) {
            return -comp;
        }
        return price.compareTo(o.getPrice());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TrackRule) {
            TrackRule other = (TrackRule) obj;
            boolean legalConfigurationsEqual = validTrackConfigurations.equals(other.validTrackConfigurations);
            boolean legalTrackPlacementEqual = validTrackPlacement.equals(other.validTrackPlacement);

            return legalConfigurationsEqual && legalTrackPlacementEqual && trackCategory.equals(other.trackCategory) && name.equals(other.name);
        }
        return false;
    }

    /**
     * @return
     */
    public TrackCategory getCategory() {
        return trackCategory;
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
        return maintenanceCost;
    }

    /**
     * @return
     */
    public Money getPrice() {
        return price;
    }

    /**
     * @return
     */
    public int getStationRadius() {
        return stationRadius;
    }

    /**
     * @return
     */
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        int result = validTrackConfigurations.hashCode();
        result = 29 * result + validTrackPlacement.hashCode();

        return result;
    }

    /**
     * @return
     */
    public boolean isStation() {
        return trackCategory.equals(TrackCategory.STATION);
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
        return getName();
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
        return doubleTracked;
    }

    /**
     * @return
     */
    public Money getFixedCost() {
        return fixedCost;
    }
}