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

import freerails.world.FullWorld;
import freerails.world.SKEY;
import freerails.world.World;
import freerails.world.cargo.CargoBatch;
import freerails.world.cargo.CargoCategory;
import freerails.world.cargo.CargoType;
import freerails.world.finances.CargoDeliveryMoneyTransaction;
import freerails.world.finances.Money;
import freerails.world.top.MapFixtureFactory;
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
        Money m = balanceSheetGenerator.mailTotal;
        assertEquals(0, m.getAmount());

        CargoType ct = (CargoType) world.get(SKEY.CARGO_TYPES, 0);
        assertEquals(CargoCategory.Mail, ct.getCategory());

        Money amount = new Money(100);
        addTrans(CargoCategory.Mail, amount);
        addTrans(CargoCategory.Passengers, amount);
        balanceSheetGenerator.calculateAll();
        m = balanceSheetGenerator.mailTotal;
        assertEquals(amount, m);
    }

    private void addTrans(CargoCategory category, Money amount) {
        for (int i = 0; i < world.size(SKEY.CARGO_TYPES); i++) {
            CargoType ct = (CargoType) world.get(SKEY.CARGO_TYPES, i);

            if (ct.getCategory() == category) {
                CargoBatch cb = new CargoBatch(i, 0, 0, 0, 0);
                world.addTransaction(MapFixtureFactory.TEST_PRINCIPAL,
                        new CargoDeliveryMoneyTransaction(amount, 10, 0, cb, 1));
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
        world = new FullWorld();
        world.addPlayer(MapFixtureFactory.TEST_PLAYER);
        MapFixtureFactory.generateCargoTypesList(world);
        balanceSheetGenerator = new IncomeStatementGenerator(world,
                MapFixtureFactory.TEST_PRINCIPAL);
    }
}