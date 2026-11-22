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

package freerails.model.operations.station;

import freerails.model.operations.cargo.CargoBatch;
import freerails.model.operations.cargo.CargoBatchBundle;
import freerails.model.simulation.Time;
import freerails.model.world.World;
import freerails.util.IntPoint2D;
import freerails.util.WorldGenerator;
import junit.framework.TestCase;

/**
 * Tests the monthly cargo calculations in {@link StationUtils}.
 */
public class StationUtilsTest extends TestCase {

    /**
     * Tests calculate amount to add behavior.
     */
    public void testCalculateAmountToAdd() {
        int amount = StationUtils.calculateAmountToAddPerMonth(12, 1);
        assertEquals(1, amount);

        assertCorrectTotalAddedOverYear(0);
        assertCorrectTotalAddedOverYear(12);
        assertCorrectTotalAddedOverYear(14);
        assertCorrectTotalAddedOverYear(140);
        assertCorrectTotalAddedOverYear(3);
    }

    /**
     * Tests that waiting cargo decays by 1/24 per month and that the station itself is left unchanged.
     */
    public void testCargoAtStationAfterMonthDecaysWithoutChangingStation() {
        World world = WorldGenerator.defaultWorld();
        int cargoTypeId = world.getCargos().iterator().next().getId();
        IntPoint2D location = new IntPoint2D(4, 5);
        CargoBatch batch = new CargoBatch(cargoTypeId, location, Time.ZERO, 1);
        CargoBatchBundle before = new CargoBatchBundle();
        before.setAmount(batch, 48);
        Station station = new Station(1, location, "Station", before);

        CargoBatchBundle after = StationUtils.calculateCargoAtStationAfterMonth(world, station);

        assertEquals(46, after.getAmount(batch));
        assertEquals(48, station.getCargoBatchBundle().getAmount(batch));
    }

    /**
     * If, say, 14 units get added each year, some month we should add 1 and
     * others we should add 2 such that over the year exactly 14 units get
     * added.
     */
    private static void assertCorrectTotalAddedOverYear(final int unitPerYear) {
        int amountAddedThisSoFar = 0;

        for (int i = 0; i < 12; i++) {
            amountAddedThisSoFar += StationUtils.calculateAmountToAddPerMonth(unitPerYear, i);
        }

        assertEquals(unitPerYear, amountAddedThisSoFar);
    }
}
