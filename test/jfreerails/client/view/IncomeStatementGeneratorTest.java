/*
 * Created on Mar 28, 2004
 */
package jfreerails.client.view;

import jfreerails.world.accounts.DeliverCargoReceipt;
import jfreerails.world.cargo.CargoBatch;
import jfreerails.world.cargo.CargoType;
import jfreerails.world.cargo.CargoType.Categories;
import jfreerails.world.common.Money;
import jfreerails.world.top.MapFixtureFactory;
import jfreerails.world.top.SKEY;
import jfreerails.world.top.World;
import jfreerails.world.top.WorldImpl;
import junit.framework.TestCase;

/**
 * JUnit tsst for IncomeStatementGenerator.
 * 
 * @author Luke
 * 
 */
public class IncomeStatementGeneratorTest extends TestCase {
    World w;

    IncomeStatementGenerator balanceSheetGenerator;

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

    @Override
    protected void setUp() throws Exception {
        w = new WorldImpl();
        w.addPlayer(MapFixtureFactory.TEST_PLAYER);
        MapFixtureFactory.generateCargoTypesList(w);
        balanceSheetGenerator = new IncomeStatementGenerator(w,
                MapFixtureFactory.TEST_PRINCIPAL);
    }
}