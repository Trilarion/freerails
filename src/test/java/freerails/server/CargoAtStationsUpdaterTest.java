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

package freerails.server;

import freerails.model.operations.cargo.CargoBatch;
import freerails.model.operations.cargo.CargoBatchBundle;
import freerails.model.operations.station.Station;
import freerails.model.operations.station.StationSupply;
import freerails.model.player.Player;
import freerails.model.simulation.Time;
import freerails.model.world.World;
import freerails.util.IntPoint2D;
import freerails.util.WorldGenerator;
import junit.framework.TestCase;

import java.util.Map;

// TODO does not exist anymore, now in FullServerGameModel, move to test there
/**
 * Test for CargoAtStationsUpdater.
 */
public class CargoAtStationsUpdaterTest extends TestCase {

    /**
     * Tests calculate amount to add behavior.
     */
    public void testCalculateAmountToAdd() {

        int amount = FullServerGameModel.calculateAmountToAddPerMonth(12, 1);
        assertEquals(1, amount);

        assertCorrectTotalAddedOverYear(0);
        assertCorrectTotalAddedOverYear(12);
        assertCorrectTotalAddedOverYear(14);
        assertCorrectTotalAddedOverYear(140);
        assertCorrectTotalAddedOverYear(3);
    }

    /**
     * Tests station production records the current simulation time.
     */
    public void testCargoProductionUsesCurrentSimulationTime() {
        World world = WorldGenerator.defaultWorld();
        Player player = WorldGenerator.TEST_PLAYER;
        world.addPlayer(player);
        int cargoTypeId = world.getCargos().iterator().next().getId();
        Station station = new Station(1, new IntPoint2D(4, 5), "Station", CargoBatchBundle.EMPTY);
        station.setSupply(new StationSupply(Map.of(cargoTypeId, 12)));
        world.addStation(player, station);

        Time creationTime = new Time(42);
        world.getClock().advanceTimeTo(creationTime);
        FullServerGameModel.cargoAtStationsUpdate(world, move -> move.apply(world));

        CargoBatch batch = new CargoBatch(cargoTypeId, station.getLocation(), creationTime, station.getId());
        assertTrue(station.getCargoBatchBundle().contains(batch));
        assertTrue(station.getCargoBatchBundle().getAmount(batch) > 0);
    }

    /**
     * If, say, 14 units get added each year, some month we should add 1 and
     * others we should add 2 such that over the year exactly 14 units get
     * added.
     */
    private static void assertCorrectTotalAddedOverYear(final int unitPerYear) {
        int amountAddedThisSoFar = 0;

        for (int i = 0; i < 12; i++) {
            amountAddedThisSoFar += FullServerGameModel.calculateAmountToAddPerMonth(unitPerYear, i);
        }

        assertEquals(unitPerYear, amountAddedThisSoFar);
    }
}