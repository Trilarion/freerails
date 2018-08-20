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

package freerails.model.track;

import freerails.io.Conceal;
import freerails.model.Identifiable;
import freerails.model.finance.Money;
import freerails.model.terrain.TerrainCategory;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Type of a track.
 */
public class TrackType extends Identifiable {

    private final String name;
    private final TrackCategory category;
    private final Set<TrackProperty> properties;
    private final Money yearlyMaintenance;
    private final Money purchasingPrice;
    private final Set<TerrainCategory> validTerrainCategories;
    private final Set<String> validTrackConfigurations;
    @Conceal private Set<TrackConfiguration> allValidTrackConfigurations;

    public TrackType(int id, String name, TrackCategory category, Set<TrackProperty> properties, Money yearlyMaintenance, Money purchasingPrice, Set<TerrainCategory> validTerrainCategories, Set<String> validTrackConfigurations) {
        super(id);
        this.name = name;
        this.category = category;
        this.properties = Collections.unmodifiableSet(properties);
        this.yearlyMaintenance = yearlyMaintenance;
        this.purchasingPrice = purchasingPrice;
        this.validTerrainCategories = Collections.unmodifiableSet(validTerrainCategories);
        this.validTrackConfigurations = Collections.unmodifiableSet(validTrackConfigurations);
    }

    /**
     * Fills allValidTrackConfigurations and checks properties. Should be called right after
     * gson deserialization.
     */
    public void prepare() {
        validateProperties();

        // rotate all track configurations and add to all list
        // TODO implement Comparable on TrackConfiguration and use TreeSet instead of HashSet
        allValidTrackConfigurations = new HashSet<>();
        for (String trackTemplateString: validTrackConfigurations) {
            int trackTemplate = Integer.parseInt(trackTemplateString, 2);
            // Check for invalid parameters.
            if ((trackTemplate > 511) || (trackTemplate < 0)) {
                throw new IllegalArgumentException("trackTemplate = " + trackTemplate + ", it should be in the range 0-511");
            }
            int[] rotationsOfTrackTemplate = EightRotationsOfTrackPieceProducer.getRotations(trackTemplate);
            for (int i: rotationsOfTrackTemplate) {
                TrackConfiguration trackConfiguration = TrackConfiguration.from9bitTemplate(i);
                allValidTrackConfigurations.add(trackConfiguration);
            }
        }
    }

    private void validateProperties() {
        // must be single or double but not both
        if (hasProperty(TrackProperty.SINGLE)) {
            if (hasProperty(TrackProperty.DOUBLE)) {
                throw new IllegalStateException("Track type cannot be single and double.");
            }
        } else {
            if (!hasProperty(TrackProperty.DOUBLE)) {
                throw new IllegalStateException("Track type must be single or double.");
            }
        }
        // cannot be tunnel and bridge
        if (hasProperty(TrackProperty.TUNNEL) && hasProperty(TrackProperty.BRIDGE)) {
            throw new IllegalStateException("Track type cannot be bridge and tunnel.");
        }
        // maintenance and purchase price must be non-negative
        if (!(yearlyMaintenance.isNonNegative() && purchasingPrice.isNonNegative())) {
            throw new IllegalStateException("Maintenance and purchase price must be non-negative.");
        }
    }

    public String getName() {
        return name;
    }

    /**
     *
     * @param property
     * @return
     */
    public boolean hasProperty(@NotNull TrackProperty property) {
        return properties.contains(property);
    }

    public boolean validTerrainCategory(TerrainCategory terrainCategory) {
        return validTerrainCategories.contains(terrainCategory);
    }

    public Set<TrackConfiguration> getValidTrackConfigurations() {
        return allValidTrackConfigurations;
    }

    public boolean isValidTrackConfiguration(TrackConfiguration configuration) {
        return allValidTrackConfigurations.contains(configuration);
    }

    public TrackCategory getCategory() {
        return category;
    }

    public Money getYearlyMaintenance() {
        return yearlyMaintenance;
    }

    public Money getPurchasingPrice() {
        return purchasingPrice;
    }

    public int getStationRadius() {
        switch (name) {
            case "depot":
                return 1;
            case "station":
                return 2;
            case "terminal":
                return 3;
            default:
                throw new RuntimeException("Not a station");
        }
    }

    public boolean isStation() {
        return category.equals(TrackCategory.STATION);
    }

    public boolean isDouble() {
        return hasProperty(TrackProperty.DOUBLE);
    }

    public boolean trackPieceIsLegal(TrackConfiguration config) {
        return allValidTrackConfigurations.contains(config);
    }

    public boolean testTrackPieceLegality(int a9bitTemplate) {
        TrackConfiguration trackConfiguration = TrackConfiguration.from9bitTemplate(a9bitTemplate);
        return trackPieceIsLegal(trackConfiguration);
    }

    public boolean canBuildOnThisTerrainType(TerrainCategory terrainCategory) {
        return validTerrainCategories.contains(terrainCategory);
    }
}
