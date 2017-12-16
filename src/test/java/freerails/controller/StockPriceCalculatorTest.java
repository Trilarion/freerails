/*
 * Created on 19-Sep-2005
 *
 */
package freerails.controller;

import freerails.server.MapFixtureFactory2;
import freerails.world.accounts.DeliverCargoReceipt;
import freerails.world.accounts.Transaction;
import freerails.world.cargo.CargoBatch;
import freerails.world.common.GameCalendar;
import freerails.world.common.GameTime;
import freerails.world.common.Money;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.top.ITEM;
import freerails.world.top.World;
import junit.framework.TestCase;

public class StockPriceCalculatorTest extends TestCase {

    World w;

    StockPriceCalculator calc;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        w = MapFixtureFactory2.getCopy();
        calc = new StockPriceCalculator(w);
    }

    /*
     * Test method for
     * 'freerails.controller.StockPriceCalculator.isFirstYear(int)'
     */
    public void testIsFirstYear() {
        assertTrue(calc.isFirstYear(0));
        GameCalendar calendar = (GameCalendar) w.get(ITEM.CALENDAR);
        int tpy = calendar.getTicksPerYear();
        int currentTicks = w.currentTime().getTicks();
        GameTime newTime = new GameTime(currentTicks + tpy + 1);
        w.setTime(newTime);
        assertFalse(calc.isFirstYear(0));
        newTime = new GameTime(currentTicks + tpy - 1);
        w.setTime(newTime);
        assertTrue(calc.isFirstYear(0));
    }

    /*
     * Test method for
     * 'freerails.controller.StockPriceCalculator.netWorth(int)'
     */
    public void testNetWorth() {
        long initialNetworth = 500000;
        assertEquals(initialNetworth, calc.netWorth(0));
        int currentTicks = w.currentTime().getTicks();
        GameTime newTime = new GameTime(currentTicks + 1);
        w.setTime(newTime);
        CargoBatch batch = new CargoBatch(0, 0, 0, 0, 0);
        long income = 100000;
        Transaction t = new DeliverCargoReceipt(new Money(income), 10, 0,
                batch, 0);
        FreerailsPrincipal princ = w.getPlayer(0).getPrincipal();
        w.addTransaction(princ, t);
        assertEquals(initialNetworth, calc.netWorth(0));

        GameCalendar calendar = (GameCalendar) w.get(ITEM.CALENDAR);
        int tpy = calendar.getTicksPerYear();
        currentTicks = w.currentTime().getTicks();
        newTime = new GameTime(currentTicks + tpy);
        w.setTime(newTime);

        long expectedNetWorth = initialNetworth + income;
        assertEquals(expectedNetWorth, calc.netWorth(0));

    }

    private void advanceTimeOneTick() {
        int currentTicks = w.currentTime().getTicks();
        GameTime newTime = new GameTime(currentTicks + 1);
        w.setTime(newTime);

    }

    private void advanceTimeOneYear() {
        GameCalendar calendar = (GameCalendar) w.get(ITEM.CALENDAR);
        int tpy = calendar.getTicksPerYear();
        int currentTicks = w.currentTime().getTicks();
        GameTime newTime = new GameTime(currentTicks + tpy);
        w.setTime(newTime);

    }

    /*
     * Test method for
     * 'freerails.controller.StockPriceCalculator.profitsLastYear(int)'
     */
    public void testProfitsLastYear() {
        assertEquals(0, calc.profitsLastYear(0));
        int currentTicks = w.currentTime().getTicks();
        GameTime newTime = new GameTime(currentTicks + 10);
        w.setTime(newTime);
        assertEquals(0, calc.profitsLastYear(0));

        long income = 100000;
        addIncome(income);
        assertEquals(0, calc.profitsLastYear(0));
        advanceTimeOneYear();
        assertEquals(income, calc.profitsLastYear(0));

    }

    private void addIncome(long income) {
        CargoBatch batch = new CargoBatch(0, 0, 0, 0, 0);

        Transaction t = new DeliverCargoReceipt(new Money(income), 10, 0,
                batch, 0);
        FreerailsPrincipal princ = w.getPlayer(0).getPrincipal();
        w.addTransaction(princ, t);
    }

    public void testCalculate() {
        Money stockPrice = calc.calculate()[0].currentPrice;
        assertEquals(new Money(10), stockPrice);
        advanceTimeOneTick();
        addIncome(100000);
        calc.calculate();
        stockPrice = calc.calculate()[0].currentPrice;
        assertEquals(new Money(10), stockPrice);
        advanceTimeOneYear();
        calc.calculate();
        stockPrice = calc.calculate()[0].currentPrice;
        assertEquals(new Money(11), stockPrice);
    }

}