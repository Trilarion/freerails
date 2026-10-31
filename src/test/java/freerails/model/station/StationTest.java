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

package freerails.model.station;

import freerails.model.cargo.CargoBatchBundle;
import freerails.model.train.TrainTemplate;
import freerails.util.Utils;
import freerails.util.Vec2D;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;

public class StationTest extends TestCase {

    public void testCargoPoliciesAndProductionAreCopiedFromInputs() {
        Map<Integer, Integer> supplyValues = new HashMap<>(Map.of(42, 3, 17, 5));
        Set<Integer> demandValues = new HashSet<>(Set.of(42));
        Map<Integer, Integer> conversionValues = new HashMap<>(Map.of(10, 20));
        List<TrainTemplate> productionValues = new ArrayList<>(List.of(new TrainTemplate(4, List.of(7))));

        Station station = new Station(1, Vec2D.ZERO, "Central", CargoBatchBundle.EMPTY);
        station.setSupply(new StationSupply(supplyValues));
        station.setDemandForCargo(new StationDemand(demandValues));
        station.setCargoConversion(new StationCargoConversion(conversionValues));
        station.setProduction(productionValues);

        supplyValues.put(42, 9);
        demandValues.remove(42);
        conversionValues.put(10, 8);
        productionValues.clear();

        assertEquals(3, station.getSupply().getSupply(42));
        assertTrue(station.getDemandForCargo().isCargoDemanded(42));
        assertEquals(OptionalInt.of(20), station.getCargoConversion().conversionFor(10));
        assertEquals(List.of(new TrainTemplate(4, List.of(7))), station.getProduction());
    }

    private void assertImmutable(Set<Integer> values) {
        try {
            values.clear();
            fail("Expected the cargo policy record component to be immutable");
        } catch (UnsupportedOperationException expected) {
            // Record components expose immutable snapshots.
        }
    }

    private void assertImmutable(Map<Integer, Integer> values) {
        try {
            values.clear();
            fail("Expected the cargo policy record component to be immutable");
        } catch (UnsupportedOperationException expected) {
            // Record components expose immutable snapshots.
        }
    }

    public void testCargoPolicyRecordComponentsAreImmutableCopies() {
        Map<Integer, Integer> supplyValues = new HashMap<>(Map.of(42, 3, 17, 5));
        Set<Integer> demandValues = new HashSet<>(Set.of(42));
        Map<Integer, Integer> conversionValues = new HashMap<>(Map.of(10, 20));

        StationSupply supply = new StationSupply(supplyValues);
        StationDemand demand = new StationDemand(demandValues);
        StationCargoConversion conversion = new StationCargoConversion(conversionValues);

        supplyValues.put(42, 9);
        demandValues.remove(42);
        conversionValues.put(10, 8);

        assertEquals(Map.of(42, 3, 17, 5), supply.supplyByCargoId());
        assertEquals(Set.of(42), demand.demandedCargoIds());
        assertEquals(Map.of(10, 20), conversion.conversions());
        assertEquals(OptionalInt.empty(), conversion.conversionFor(11));

        assertImmutable(supply.supplyByCargoId());
        assertImmutable(demand.demandedCargoIds());
        try {
            conversion.conversions().clear();
            fail("Expected the conversion map to be immutable");
        } catch (UnsupportedOperationException expected) {
            // Conversion settings expose an immutable snapshot.
        }
    }

    public void testCargoConversionSurvivesSerialization() {
        StationCargoConversion conversion = new StationCargoConversion(Map.of(10, 20));

        StationCargoConversion copy = (StationCargoConversion) Utils.cloneBySerialisation(conversion);

        assertEquals(conversion, copy);
        assertEquals(conversion.hashCode(), copy.hashCode());
        assertEquals(OptionalInt.of(20), copy.conversionFor(10));
    }

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

    public void testStationCargoPoliciesSurviveSerialization() {
        Station station = new Station(1, Vec2D.ZERO, "Central", CargoBatchBundle.EMPTY);
        station.setSupply(new StationSupply(Map.of(42, 3)));
        station.setDemandForCargo(new StationDemand(Set.of(42)));

        Station copy = (Station) Utils.cloneBySerialisation(station);

        assertEquals(3, copy.getSupply().getSupply(42));
        assertTrue(copy.getDemandForCargo().isCargoDemanded(42));
    }

    public void testProductionGetterCannotMutateStationPolicy() {
        Station station = new Station(1, Vec2D.ZERO, "Central", CargoBatchBundle.EMPTY);
        station.setProduction(new ArrayList<>(List.of(new TrainTemplate(4, List.of(7)))));

        try {
            station.getProduction().clear();
            fail("Expected the production policy to be immutable");
        } catch (UnsupportedOperationException expected) {
            // The station exposes an immutable production snapshot.
        }

        assertEquals(List.of(new TrainTemplate(4, List.of(7))), station.getProduction());
    }

    public void testProductionQueueOperationsAllowDuplicatesAndRemoveFirstMatch() {
        Station station = new Station(1, Vec2D.ZERO, "Central", CargoBatchBundle.EMPTY);
        TrainTemplate first = new TrainTemplate(4, List.of(7));
        TrainTemplate second = new TrainTemplate(5, List.of(8));

        station.queueProduction(first);
        List<TrainTemplate> productionView = station.getProduction();
        station.queueProduction(first);
        station.queueProduction(second);

        assertEquals(List.of(first, first, second), productionView);
        assertTrue(station.removeProduction(first));
        assertEquals(List.of(first, second), station.getProduction());
        assertTrue(station.removeProduction(first));
        assertFalse(station.removeProduction(first));
        assertEquals(List.of(second), station.getProduction());

        station.setProduction(List.of(first));
        assertEquals(List.of(first), productionView);
    }
}
