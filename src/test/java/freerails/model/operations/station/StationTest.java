/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the FreeRails Team, either version 3 of the License, or
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

package freerails.model.operations.station;

import freerails.model.operations.cargo.CargoBatchBundle;
import freerails.util.Utils;
import freerails.util.IntPoint2D;
import junit.framework.TestCase;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tests station behavior.
 */
public class StationTest extends TestCase {

    /**
     * Tests cargo policies are copied from inputs behavior.
     */
    public void testCargoPoliciesAreCopiedFromInputs() {
        Map<Integer, Integer> supplyValues = new HashMap<>(Map.of(42, 3, 17, 5));
        Set<Integer> demandValues = new HashSet<>(Set.of(42));

        Station station = new Station(1, IntPoint2D.ZERO, "Central", CargoBatchBundle.EMPTY);
        station.setSupply(new StationSupply(supplyValues));
        station.setDemandForCargo(new StationDemand(demandValues));

        supplyValues.put(42, 9);
        demandValues.remove(42);

        assertEquals(3, station.getSupply().getSupply(42));
        assertTrue(station.getDemandForCargo().isCargoDemanded(42));
    }

    /**
     * Asserts immutable.
     * @param values the values to use in the test.
     */
    private void assertImmutable(Set<Integer> values) {
        try {
            values.clear();
            fail("Expected the cargo policy record component to be immutable");
        } catch (UnsupportedOperationException expected) {
            // Record components expose immutable snapshots.
        }
    }

    /**
     * Asserts immutable.
     * @param values the values to use in the test.
     */
    private void assertImmutable(Map<Integer, Integer> values) {
        try {
            values.clear();
            fail("Expected the cargo policy record component to be immutable");
        } catch (UnsupportedOperationException expected) {
            // Record components expose immutable snapshots.
        }
    }

    /**
     * Tests cargo policy record components are immutable copies behavior.
     */
    public void testCargoPolicyRecordComponentsAreImmutableCopies() {
        Map<Integer, Integer> supplyValues = new HashMap<>(Map.of(42, 3, 17, 5));
        Set<Integer> demandValues = new HashSet<>(Set.of(42));
        StationSupply supply = new StationSupply(supplyValues);
        StationDemand demand = new StationDemand(demandValues);

        supplyValues.put(42, 9);
        demandValues.remove(42);

        assertEquals(Map.of(42, 3, 17, 5), supply.supplyByCargoId());
        assertEquals(Set.of(42), demand.demandedCargoIds());

        assertImmutable(supply.supplyByCargoId());
        assertImmutable(demand.demandedCargoIds());
    }

    /**
     * Tests cargo policies stay associated with ids when catalogue order changes behavior.
     */
    public void testCargoPoliciesStayAssociatedWithIdsWhenCatalogueOrderChanges() {
        StationSupply supply = new StationSupply(Map.of(42, 3, 17, 5));
        StationDemand demand = new StationDemand(Set.of(42));
        Map<Integer, Integer> expectedSupplyById = Map.of(42, 3, 17, 5);
        List<List<Integer>> catalogueOrders = List.of(List.of(42, 17), List.of(17, 42));

        for (List<Integer> cargoOrder : catalogueOrders) {
            for (int cargoId : cargoOrder) {
                assertEquals(expectedSupplyById.get(cargoId).intValue(), supply.getSupply(cargoId));
                assertEquals(cargoId == 42, demand.isCargoDemanded(cargoId));
            }
        }
    }

    /**
     * Tests station cargo policies survive serialization behavior.
     */
    public void testStationCargoPoliciesSurviveSerialization() {
        Station station = new Station(1, IntPoint2D.ZERO, "Central", CargoBatchBundle.EMPTY);
        station.setSupply(new StationSupply(Map.of(42, 3)));
        station.setDemandForCargo(new StationDemand(Set.of(42)));

        Station copy = (Station) Utils.cloneBySerialisation(station);

        assertEquals(3, copy.getSupply().getSupply(42));
        assertTrue(copy.getDemandForCargo().isCargoDemanded(42));
    }
}
