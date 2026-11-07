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

import freerails.model.economy.Money;
import freerails.model.terrain.TerrainCategory;
import junit.framework.TestCase;

import java.util.HashSet;
import java.util.Set;

/**
 * Tests track type behavior.
 */
public class TrackTypeTest extends TestCase {

    /**
     * Tests definition collections are copied and prepared configurations are immutable behavior.
     */
    public void testDefinitionCollectionsAreCopiedAndPreparedConfigurationsAreImmutable() {
        Set<TrackProperty> properties = new HashSet<>(Set.of(TrackProperty.SINGLE));
        Set<TerrainCategory> validTerrainCategories = new HashSet<>(Set.of(TerrainCategory.COUNTRY));
        Set<String> validTrackConfigurations = new HashSet<>(Set.of("00000001"));
        TrackType trackType = new TrackType(1, "single", TrackCategory.TRACK, properties,
                Money.ZERO, Money.ZERO, validTerrainCategories, validTrackConfigurations);

        properties.clear();
        properties.add(TrackProperty.DOUBLE);
        validTerrainCategories.clear();
        validTrackConfigurations.clear();
        validTrackConfigurations.add("11111111");

        assertTrue(trackType.hasProperty(TrackProperty.SINGLE));
        assertFalse(trackType.hasProperty(TrackProperty.DOUBLE));
        assertTrue(trackType.canBuildOnThisTerrainType(TerrainCategory.COUNTRY));

        trackType.prepare();

        TrackConfiguration validConfiguration = TrackConfiguration.from8bitTemplate(0b00000001);
        assertTrue(trackType.trackPieceIsLegal(validConfiguration));
        assertFalse(trackType.trackPieceIsLegal(TrackConfiguration.from8bitTemplate(0b11111111)));
        assertThrowsUnsupportedOperation(() -> trackType.getValidTrackConfigurations().clear());
    }

    /**
     * Tests track types reject configurations with no directional connections.
     */
    public void testEmptyConfigurationIsRejected() {
        TrackType trackType = new TrackType(1, "single", TrackCategory.TRACK,
                Set.of(TrackProperty.SINGLE), Money.ZERO, Money.ZERO,
                Set.of(TerrainCategory.COUNTRY), Set.of("00000000"));

        try {
            trackType.prepare();
            fail("Expected empty track configuration to be rejected.");
        } catch (IllegalArgumentException expected) {
            // Track types must define at least one direction.
        }
    }

    /**
     * Asserts throws unsupported operation.
     * @param action the action to use in the test.
     */
    private static void assertThrowsUnsupportedOperation(Runnable action) {
        try {
            action.run();
            fail("Expected an immutable configuration set");
        } catch (UnsupportedOperationException expected) {
            // Track type definitions expose immutable configuration sets.
        }
    }
}
