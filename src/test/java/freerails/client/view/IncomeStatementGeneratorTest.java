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

import freerails.world.cargo.CargoBatch;
import freerails.world.cargo.CargoType;
import freerails.world.cargo.CargoType.Categories;
import freerails.world.finances.DeliverCargoReceipt;
import freerails.world.finances.Money;
import freerails.world.top.MapFixtureFactory;
import freerails.world.top.SKEY;
import freerails.world.top.World;
import freerails.world.top.WorldImpl;
import junit.framework.TestCase;

/**
 * JUnit tsst for IncomeStatementGenerator.
 */
public class IncomeStatementGeneratorTest extends TestCase {
    World w;

    IncomeStatementGenerator balanceSheetGenerator;

    /**
     *
     */
    public void testCalExpense() {
        balanceSheetGenerator.calculateAll();
        Money m = balanceSheetGenerator.mailTotal;
        assertEquals(0, m.getAmount());

        CargoType ct = (CargoType) w.get(SKEY.CARGO_TYPES, 0);
        assertEquals(Categories.Mail, ct.getCategory());

        Money amount = new Money(100);
        addTrans(Categories.Mail, amount);
        addTrans(Categories.Passengers, amount);
        balanceSheetGenerator.calculateAll();
        m = balanceSheetGenerator.mailTotal;
        assertEquals(amount, m);
    }

    private void addTrans(Categories category, Money amount) {
        for (int i = 0; i < w.size(SKEY.CARGO_TYPES); i++) {
            CargoType ct = (CargoType) w.get(SKEY.CARGO_TYPES, i);

            if (ct.getCategory().equals(category)) {
                CargoBatch cb = new CargoBatch(i, 0, 0, 0, 0);
                w.addTransaction(MapFixtureFactory.TEST_PRINCIPAL,
                        new DeliverCargoReceipt(amount, 10, 0, cb, 1));
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
        w = new WorldImpl();
        w.addPlayer(MapFixtureFactory.TEST_PLAYER);
        MapFixtureFactory.generateCargoTypesList(w);
        balanceSheetGenerator = new IncomeStatementGenerator(w,
                MapFixtureFactory.TEST_PRINCIPAL);
    }
}