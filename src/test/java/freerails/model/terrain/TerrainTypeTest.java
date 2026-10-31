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

/*
 *
 */
package freerails.model.terrain;

import freerails.io.GsonManager;
import freerails.model.cargo.CargoConversion;
import freerails.model.cargo.CargoProductionOrConsumption;
import freerails.model.finance.Money;
import freerails.util.TestUtils;
import freerails.util.Utils;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 */
public class TerrainTypeTest extends TestCase {

    /**
     *
     */
    public void testTileTypeImpl() {
        List<CargoProductionOrConsumption> productions = asList(new CargoProductionOrConsumption(69, 10));
        List<CargoProductionOrConsumption> consumptions = asList(new CargoProductionOrConsumption(4, 4), new CargoProductionOrConsumption(4, 5));
        List<CargoConversion> conversions1 = asList(new CargoConversion(50, 30, 1));

        TestUtils.assertCloneBySerializationBehavesWell(productions.get(0));
        TestUtils.assertCloneBySerializationBehavesWell(consumptions.get(0));
        TestUtils.assertCloneBySerializationBehavesWell(conversions1.get(0));

        Terrain terrainType1 = new Terrain(1, "Grassland", TerrainCategory.COUNTRY, new Money(100), new Money(10), productions, conversions1, consumptions);

        TestUtils.assertCloneBySerializationBehavesWell(terrainType1);
    }

    public void testConstructionCostsHaveSeparatePurposes() {
        Terrain terrain = new Terrain(1, "Factory", TerrainCategory.INDUSTRY,
                new Money(1000), new Money(600000), List.of(), List.of(), List.of());

        assertEquals(new Money(1000), terrain.getTrackConstructionCostPerMile());
        assertEquals(new Money(600000), terrain.getIndustryBuildCost());
    }

    public void testLegacyTerrainCostJsonKeysRemainReadable() throws Exception {
        Path file = Files.createTempFile("terrain-costs", ".json");
        try {
            Files.writeString(file, "{\"id\":1,\"name\":\"Factory\",\"category\":\"INDUSTRY\","
                    + "\"rightOfWayCost\":1000,\"buildCost\":600000,"
                    + "\"productions\":[],\"conversions\":[],\"consumptions\":[]}");

            Terrain terrain = GsonManager.load(file.toFile(), Terrain.class);

            assertEquals(new Money(1000), terrain.getTrackConstructionCostPerMile());
            assertEquals(new Money(600000), terrain.getIndustryBuildCost());

            GsonManager.save(file.toFile(), terrain);
            String savedJson = Files.readString(file);
            assertTrue(savedJson.contains("\"trackConstructionCostPerMile\""));
            assertTrue(savedJson.contains("\"industryBuildCost\""));
        } finally {
            Files.deleteIfExists(file);
        }
    }

    public void testCargoPoliciesAreCopiedAndCannotBeChangedThroughGetters() {
        List<CargoProductionOrConsumption> productions =
                new ArrayList<>(List.of(new CargoProductionOrConsumption(1, 2)));
        List<CargoConversion> conversions = new ArrayList<>(List.of(new CargoConversion(1, 2, 0.5)));
        List<CargoProductionOrConsumption> consumptions =
                new ArrayList<>(List.of(new CargoProductionOrConsumption(2, 3)));
        Terrain terrain = new Terrain(1, "Grassland", TerrainCategory.COUNTRY,
                Money.ZERO, Money.ZERO, productions, conversions, consumptions);

        productions.clear();
        productions.add(new CargoProductionOrConsumption(4, 5));
        conversions.clear();
        consumptions.clear();

        assertEquals(List.of(new CargoProductionOrConsumption(1, 2)), terrain.getProductions());
        assertEquals(List.of(new CargoConversion(1, 2, 0.5)), terrain.getConversions());
        assertEquals(List.of(new CargoProductionOrConsumption(2, 3)), terrain.getConsumptions());
        assertThrowsUnsupportedOperation(() -> terrain.getProductions().clear());
        assertThrowsUnsupportedOperation(() -> terrain.getConversions().clear());
        assertThrowsUnsupportedOperation(() -> terrain.getConsumptions().clear());
    }

    private static void assertThrowsUnsupportedOperation(Runnable action) {
        try {
            action.run();
            fail("Expected an immutable policy list");
        } catch (UnsupportedOperationException expected) {
            // Policy getters expose immutable snapshots.
        }
    }

    @SafeVarargs
    public static <E> List<E> asList(E... e) {
        Utils.verifyNotNull(e);
        return Collections.unmodifiableList(Arrays.asList(e));
    }


}
