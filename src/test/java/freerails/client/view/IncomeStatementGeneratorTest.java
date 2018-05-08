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

import freerails.io.GsonManager;
import freerails.model.cargo.CargoCategory;
import freerails.model.cargo.Cargo;
import freerails.model.finances.IncomeStatementGenerator;
import freerails.util.Vec2D;
import freerails.model.world.World;
import freerails.model.cargo.CargoBatch;
import freerails.model.finances.CargoDeliveryMoneyTransaction;
import freerails.model.finances.Money;
import freerails.model.MapFixtureFactory;
import junit.framework.TestCase;

import java.io.File;
import java.net.URL;
import java.util.SortedSet;

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
        assertEquals(0, m.amount);

        Cargo ct = world.getCargoType(0);
        assertEquals(CargoCategory.MAIL, ct.getCategory());

        Money amount = new Money(100);
        addTrans(CargoCategory.MAIL, amount);
        addTrans(CargoCategory.PASSENGER, amount);
        balanceSheetGenerator.calculateAll();
        m = balanceSheetGenerator.mailTotal;
        assertEquals(amount, m);
    }

    private void addTrans(CargoCategory category, Money amount) {
        // TODO i is not an id
        for (int i = 0; i < world.getCargos().size(); i++) {
            Cargo ct = world.getCargoType(i);

            if (ct.getCategory() == category) {
                CargoBatch cb = new CargoBatch(i, Vec2D.ZERO, 0, 0);
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
        super.setUp();

        // load cargo types
        URL url = IncomeStatementGenerator.class.getResource("/freerails/data/scenario/cargo_types.json");
        File file = new File(url.toURI());
        SortedSet<Cargo> cargos = GsonManager.loadCargoTypes(file);

        world = new World.Builder().setCargos(cargos).build();
        world.addPlayer(MapFixtureFactory.TEST_PLAYER);
        balanceSheetGenerator = new IncomeStatementGenerator(world, MapFixtureFactory.TEST_PRINCIPAL);
    }
}