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

package freerails.move;

import freerails.model.building.EngineShop;
import freerails.model.operations.cargo.CargoBatchBundle;
import freerails.model.economy.Money;
import freerails.model.economy.transaction.Transaction;
import freerails.model.economy.transaction.TransactionCategory;
import freerails.model.operations.station.Station;
import freerails.util.IntPoint2D;
import freerails.util.WorldGenerator;

/**
 * Tests build engine shop move behavior.
 */
public class BuildEngineShopMoveTest extends AbstractMoveTestCase {

    private final IntPoint2D location = new IntPoint2D(4, 5);

    /**
     * Sets up.
     * @throws Exception if the operation fails.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        getWorld().addStation(WorldGenerator.TEST_PLAYER,
                new Station(0, location, "Central", CargoBatchBundle.EMPTY));
        getWorld().addTransaction(WorldGenerator.TEST_PLAYER,
                new Transaction(TransactionCategory.MISC_INCOME,
                        BuildEngineShopMove.BUILD_COST, getWorld().getClock().getCurrentTime()));
    }

    /**
     * Tests build engine shop charges and registers facility behavior.
     */
    public void testBuildEngineShopChargesAndRegistersFacility() {
        BuildEngineShopMove move = new BuildEngineShopMove(WorldGenerator.TEST_PLAYER,
                new EngineShop(0, location));
        assertEquals(Status.OK, move.applicable(getWorld()));
        move.apply(getWorld());

        assertEquals(1, getWorld().getEngineShops(WorldGenerator.TEST_PLAYER).size());
        assertEquals(location, getWorld().getEngineShop(WorldGenerator.TEST_PLAYER, 0).getLocation());
        assertEquals(Money.ZERO, getWorld().getCurrentBalance(WorldGenerator.TEST_PLAYER));
        assertEquals(TransactionCategory.STATIONS,
                getWorld().getTransaction(WorldGenerator.TEST_PLAYER, 1).getCategory());
    }

    /**
     * Tests build engine shop requires station and funds behavior.
     */
    public void testBuildEngineShopRequiresStationAndFunds() {
        BuildEngineShopMove noStation = new BuildEngineShopMove(WorldGenerator.TEST_PLAYER,
                new EngineShop(0, IntPoint2D.ZERO));
        assertFalse(noStation.applicable(getWorld()).isSuccess());

        getWorld().removeStation(WorldGenerator.TEST_PLAYER, 0);
        getWorld().addStation(WorldGenerator.TEST_PLAYER,
                new Station(0, location, "Central", CargoBatchBundle.EMPTY));
        getWorld().addTransaction(WorldGenerator.TEST_PLAYER,
                new Transaction(TransactionCategory.MISC_INCOME,
                        new Money(-BuildEngineShopMove.BUILD_COST.amount()), getWorld().getClock().getCurrentTime()));
        BuildEngineShopMove noFunds = new BuildEngineShopMove(WorldGenerator.TEST_PLAYER,
                new EngineShop(0, location));
        assertFalse(noFunds.applicable(getWorld()).isSuccess());
    }
}
