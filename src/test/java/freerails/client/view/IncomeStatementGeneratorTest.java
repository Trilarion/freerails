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

import freerails.model.operations.cargo.CargoCategory;
import freerails.model.operations.cargo.Cargo;
import freerails.model.economy.reporting.IncomeStatementGenerator;
import freerails.util.Vec2D;
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

    private World world;
    private IncomeStatementGenerator balanceSheetGenerator;

    /**
     * Tests cal expense behavior.
     */
    public void testCalExpense() {
        balanceSheetGenerator.calculateAll();
        Money money = balanceSheetGenerator.mailTotal;
        assertEquals(0, money.amount());

        Cargo cargo = world.getCargo(0);
        assertEquals(CargoCategory.MAIL, cargo.getCategory());

        Money amount = new Money(100);
        addTrans(CargoCategory.MAIL, amount);
        addTrans(CargoCategory.PASSENGER, amount);
        balanceSheetGenerator.calculateAll();
        money = balanceSheetGenerator.mailTotal;
        assertEquals(amount, money);
    }

    /**
     * Adds trans.
     * @param category the category used by this test.
     * @param amount the amount used by this test.
     */
    private void addTrans(CargoCategory category, Money amount) {
        // TODO i is not an id
        for (int i = 0; i < world.getCargos().size(); i++) {
            Cargo ct = world.getCargo(i);

            if (ct.getCategory() == category) {
                CargoBatch cb = new CargoBatch(i, Vec2D.ZERO, 0, 0);
                world.addTransaction(WorldGenerator.TEST_PLAYER,
                        new CargoDeliveryTransaction(amount, world.getClock().getCurrentTime(), 10, 0, 1, cb));
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
        balanceSheetGenerator = new IncomeStatementGenerator(world, WorldGenerator.TEST_PLAYER);
    }
}