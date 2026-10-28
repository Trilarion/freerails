/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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
import freerails.util.Identifiable;
import freerails.model.finance.Money;
import freerails.model.terrain.TerrainCategory;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Defines a buildable track type, including its cost, terrain restrictions,
 * geometry rules, and optional station properties.
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

    /**
     * Creates a track type from its persistent definition.
     *
     * @param id unique track type ID
     * @param name display name
     * @param category broad category of track
     * @param properties track properties such as single/double or bridge
     * @param yearlyMaintenance annual maintenance cost
     * @param purchasingPrice purchase/build price
     * @param validTerrainCategories terrain categories on which it may be built
     * @param validTrackConfigurations binary templates valid for this type
     */
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
     * Validates this type and derives its legal configurations by rotating each
     * configured template. Call after deserializing the persistent definition.
     *
     * @throws IllegalStateException if the properties or prices are invalid
     * @throws IllegalArgumentException if a configured template is outside 0 through 511
     * @throws NumberFormatException if a configured template is not binary
     */
    public void prepare() {
        validateProperties();

        // rotate all track configurations and add to all list
        // TODO implement Comparable on TrackConfiguration and use TreeSet instead of HashSet
        allValidTrackConfigurations = new HashSet<>();
        for (String trackTemplateString: validTrackConfigurations) {
            int trackTemplate = Integer.parseInt(trackTemplateString, 2);
            // Check for invalid parameters.
            if (trackTemplate > 511 || trackTemplate < 0) {
                throw new IllegalArgumentException("trackTemplate = " + trackTemplate + ", it should be in the range 0-511");
            }
            int[] rotationsOfTrackTemplate = EightRotationsOfTrackPieceProducer.getRotations(trackTemplate);
            for (int i: rotationsOfTrackTemplate) {
                TrackConfiguration trackConfiguration = TrackConfiguration.from9bitTemplate(i);
                allValidTrackConfigurations.add(trackConfiguration);
            }
        }
    }

    /**
     * Checks required property combinations and non-negative prices.
     *
     * @throws IllegalStateException if properties conflict or a price is negative
     */
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

    /**
     * Returns this type's display name.
     *
     * @return track type name
     */
    public String getName() {
        return name;
    }

    /**
     * Tests whether this type has a property.
     *
     * @param property property to test
     * @return {@code true} if the type has {@code property}
     */
    public boolean hasProperty(@NotNull TrackProperty property) {
        return properties.contains(property);
    }

    /**
     * Returns the legal configurations computed by {@link #prepare()}.
     *
     * @return legal configurations, including their rotations
     */
    public Set<TrackConfiguration> getValidTrackConfigurations() {
        return allValidTrackConfigurations;
    }

    /**
     * Returns this type's broad category.
     *
     * @return track category
     */
    public TrackCategory getCategory() {
        return category;
    }

    /**
     * Returns the annual maintenance cost for this type.
     *
     * @return annual maintenance amount
     */
    public Money getYearlyMaintenance() {
        return yearlyMaintenance;
    }

    /**
     * Returns the purchase price for this type.
     *
     * @return purchasing price
     */
    public Money getPurchasingPrice() {
        return purchasingPrice;
    }

    /**
     * Returns the station's exclusion radius based on its name.
     *
     * @return radius for depot, station, or terminal
     * @throws RuntimeException if this type is not a recognized station type
     */
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

    /**
     * Tests whether this type belongs to the station category.
     *
     * @return {@code true} for a station type
     */
    public boolean isStation() {
        return category.equals(TrackCategory.STATION);
    }

    /**
     * Tests whether this type represents double track.
     *
     * @return {@code true} if the double-track property is present
     */
    public boolean isDouble() {
        return hasProperty(TrackProperty.DOUBLE);
    }

    /**
     * Tests whether a configuration is among this type's permitted geometries.
     *
     * @param config configuration to check
     * @return {@code true} if the configuration is legal
     */
    public boolean trackPieceIsLegal(TrackConfiguration config) {
        return allValidTrackConfigurations.contains(config);
    }

    /**
     * Tests legality of a configuration supplied as a 9-bit template.
     *
     * @param a9bitTemplate encoded track configuration
     * @return {@code true} if the configuration is legal for this type
     * @throws IndexOutOfBoundsException if the template is outside 0 through 511
     */
    public boolean testTrackPieceLegality(int a9bitTemplate) {
        TrackConfiguration trackConfiguration = TrackConfiguration.from9bitTemplate(a9bitTemplate);
        return trackPieceIsLegal(trackConfiguration);
    }

    /**
     * Tests whether this type may be built on a terrain category.
     *
     * @param terrainCategory terrain category to check
     * @return {@code true} if this category is permitted
     */
    public boolean canBuildOnThisTerrainType(TerrainCategory terrainCategory) {
        return validTerrainCategories.contains(terrainCategory);
    }
}
