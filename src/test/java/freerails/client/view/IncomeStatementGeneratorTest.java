/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
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

import freerails.model.cargo.CargoCategory;
import freerails.model.cargo.Cargo;
import freerails.model.finance.IncomeStatementGenerator;
import freerails.util.Vec2D;
import freerails.model.world.World;
import freerails.model.cargo.CargoBatch;
import freerails.model.finance.transaction.CargoDeliveryTransaction;
import freerails.model.finance.Money;
import freerails.util.WorldGenerator;
import junit.framework.TestCase;

/**
 * Test for IncomeStatementGenerator.
 */
public class IncomeStatementGeneratorTest extends TestCase {

    private World world;
    private IncomeStatementGenerator balanceSheetGenerator;

    /**
     *
     */
    public void testCalExpense() {
        balanceSheetGenerator.calculateAll();
        Money money = balanceSheetGenerator.mailTotal;
        assertEquals(0, money.amount);

        Cargo cargo = world.getCargo(0);
        assertEquals(CargoCategory.MAIL, cargo.getCategory());

        Money amount = new Money(100);
        addTrans(CargoCategory.MAIL, amount);
        addTrans(CargoCategory.PASSENGER, amount);
        balanceSheetGenerator.calculateAll();
        money = balanceSheetGenerator.mailTotal;
        assertEquals(amount, money);
    }

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
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        world = WorldGenerator.defaultWorld();
        world.addPlayer(WorldGenerator.TEST_PLAYER);
        balanceSheetGenerator = new IncomeStatementGenerator(world, WorldGenerator.TEST_PLAYER);
    }
}