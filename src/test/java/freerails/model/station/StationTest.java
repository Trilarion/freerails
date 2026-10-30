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
import freerails.util.Vec2D;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

public class StationTest extends TestCase {

    public void testCargoPoliciesAndProductionAreCopiedFromInputs() {
        Integer[] supplyValues = {3, 5};
        boolean[] demandValues = {true, false};
        Integer[] conversionValues = {1, Integer.MIN_VALUE};
        List<TrainTemplate> productionValues = new ArrayList<>(List.of(new TrainTemplate(4, List.of(7))));

        Station station = new Station(1, Vec2D.ZERO, "Central", 2, CargoBatchBundle.EMPTY);
        station.setSupply(new StationSupply(supplyValues));
        station.setDemandForCargo(new StationDemand(demandValues));
        station.setCargoConversion(new StationCargoConversion(conversionValues));
        station.setProduction(productionValues);

        supplyValues[0] = 9;
        demandValues[0] = false;
        conversionValues[0] = 8;
        productionValues.clear();

        assertEquals(3, station.getSupply().getSupply(0));
        assertTrue(station.getDemandForCargo().isCargoDemanded(0));
        assertEquals(1, station.getCargoConversion().getConversion(0));
        assertEquals(List.of(new TrainTemplate(4, List.of(7))), station.getProduction());
    }

    public void testCargoPolicyRecordComponentsAreImmutableCopies() {
        List<Integer> supplyValues = new ArrayList<>(List.of(3, 5));
        List<Integer> demandValues = new ArrayList<>(List.of(1, 0));
        List<Integer> conversionValues = new ArrayList<>(List.of(1, Integer.MIN_VALUE));

        StationSupply supply = new StationSupply(supplyValues);
        StationDemand demand = new StationDemand(demandValues);
        StationCargoConversion conversion = new StationCargoConversion(conversionValues);

        supplyValues.set(0, 9);
        demandValues.set(0, 0);
        conversionValues.set(0, 8);

        assertEquals(List.of(3, 5), supply.supply());
        assertEquals(List.of(1, 0), demand.demand());
        assertEquals(List.of(1, Integer.MIN_VALUE), conversion.convertedTo());

        assertImmutable(supply.supply());
        assertImmutable(demand.demand());
        assertImmutable(conversion.convertedTo());
    }

    private void assertImmutable(List<Integer> values) {
        try {
            values.clear();
            fail("Expected the cargo policy record component to be immutable");
        } catch (UnsupportedOperationException expected) {
            // Record components expose immutable snapshots.
        }
    }

    public void testProductionGetterCannotMutateStationPolicy() {
        Station station = new Station(1, Vec2D.ZERO, "Central", 1, CargoBatchBundle.EMPTY);
        station.setProduction(new ArrayList<>(List.of(new TrainTemplate(4, List.of(7)))));

        try {
            station.getProduction().clear();
            fail("Expected the production policy to be immutable");
        } catch (UnsupportedOperationException expected) {
            // The station exposes an immutable production snapshot.
        }

        assertEquals(List.of(new TrainTemplate(4, List.of(7))), station.getProduction());
    }
}
