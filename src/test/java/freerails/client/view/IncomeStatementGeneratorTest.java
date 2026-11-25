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

package freerails.client.view;

import freerails.model.simulation.Time;
import freerails.model.operations.cargo.CargoCategory;
import freerails.model.operations.cargo.Cargo;
import freerails.model.economy.reporting.IncomeStatementGenerator;
import freerails.util.IntPoint2D;
import freerails.model.world.World;
import freerails.model.operations.cargo.CargoBatch;
import freerails.model.economy.transaction.CargoDeliveryTransaction;
import freerails.model.economy.Money;
import freerails.util.WorldGenerator;
import junit.framework.TestCase;

/**
 * Test for IncomeStatementGenerator.
 */
public class IncomeStatementGeneratorTest extends TestCase {

    private static final int QUANTITY = 10;
    private static final int STATION_ID = 0;
    private static final int TRAIN_ID = 1;

    private World world;
    private IncomeStatementGenerator incomeStatementGenerator;

    /**
     * Tests cal expense behavior.
     */
    public void testCalExpense() {
        incomeStatementGenerator.calculateAll();
        Money money = incomeStatementGenerator.mailTotal;
        assertEquals(0, money.amount());

        Cargo cargo = world.getCargo(0);
        assertEquals(CargoCategory.MAIL, cargo.getCategory());

        Money amount = new Money(100);
        addTransaction(CargoCategory.MAIL, amount);
        addTransaction(CargoCategory.PASSENGER, amount);
        incomeStatementGenerator.calculateAll();
        money = incomeStatementGenerator.mailTotal;
        assertEquals(amount, money);
        assertEquals("100", incomeStatementGenerator.getTemplateValues().get("mailTotal"));
        assertEquals("100", incomeStatementGenerator.getTemplateValues().get("passengersTotal"));
        assertEquals("200", incomeStatementGenerator.getTemplateValues().get("profitTotal"));
    }

    /**
     * Adds a delivery of the first cargo of a category for the test player.
     * @param category the category of the delivered cargo.
     * @param amount the revenue of the delivery.
     */
    private void addTransaction(CargoCategory category, Money amount) {
        for (Cargo cargo : world.getCargos()) {
            if (cargo.getCategory() == category) {
                CargoBatch cargoBatch = new CargoBatch(cargo.getId(), IntPoint2D.ZERO, new Time(0), 0);
                world.addTransaction(WorldGenerator.TEST_PLAYER, new CargoDeliveryTransaction(
                        amount, world.getClock().getCurrentTime(), QUANTITY, STATION_ID, TRAIN_ID, cargoBatch));
                return;
            }
        }

        throw new IllegalArgumentException(category.toString());
    }

    /**
     * Initializes the fixture before each test.
     * @throws Exception if the set up operation fails.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        world = WorldGenerator.defaultWorld();
        world.addPlayer(WorldGenerator.TEST_PLAYER);
        incomeStatementGenerator = new IncomeStatementGenerator(world, WorldGenerator.TEST_PLAYER);
    }
}