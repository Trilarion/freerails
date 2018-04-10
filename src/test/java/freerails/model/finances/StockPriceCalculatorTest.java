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

/*
 *
 */
package freerails.model.finances;

import freerails.model.MapFixtureFactory2;
import freerails.util.Vec2D;
import freerails.model.game.GameCalendar;
import freerails.model.game.GameTime;
import freerails.model.world.WorldItem;
import freerails.model.world.World;
import freerails.model.cargo.CargoBatch;
import freerails.model.player.FreerailsPrincipal;
import junit.framework.TestCase;

/**
 *
 */
public class StockPriceCalculatorTest extends TestCase {

    private World world;
    private StockPriceCalculator calc;

    /**
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        world = MapFixtureFactory2.getCopy();
        calc = new StockPriceCalculator(world);
    }

    /**
     *
     */
    public void testIsFirstYear() {
        assertTrue(calc.isFirstYear(0));
        GameCalendar calendar = (GameCalendar) world.get(WorldItem.Calendar);
        int tpy = calendar.getTicksPerYear();
        int currentTicks = world.currentTime().getTicks();
        GameTime newTime = new GameTime(currentTicks + tpy + 1);
        world.setTime(newTime);
        assertFalse(calc.isFirstYear(0));
        newTime = new GameTime(currentTicks + tpy - 1);
        world.setTime(newTime);
        assertTrue(calc.isFirstYear(0));
    }

    /**
     *
     */
    public void testNetWorth() {
        long initialNetworth = 500000;
        assertEquals(initialNetworth, calc.netWorth(0));
        int currentTicks = world.currentTime().getTicks();
        GameTime newTime = new GameTime(currentTicks + 1);
        world.setTime(newTime);
        CargoBatch batch = new CargoBatch(0, Vec2D.ZERO, 0, 0);
        long income = 100000;
        Transaction transaction = new CargoDeliveryMoneyTransaction(new Money(income), 10, 0,
                batch, 0);
        FreerailsPrincipal princ = world.getPlayer(0).getPrincipal();
        world.addTransaction(princ, transaction);
        assertEquals(initialNetworth, calc.netWorth(0));

        GameCalendar calendar = (GameCalendar) world.get(WorldItem.Calendar);
        int tpy = calendar.getTicksPerYear();
        currentTicks = world.currentTime().getTicks();
        newTime = new GameTime(currentTicks + tpy);
        world.setTime(newTime);

        long expectedNetWorth = initialNetworth + income;
        assertEquals(expectedNetWorth, calc.netWorth(0));
    }

    /**
     *
     */
    private void advanceTimeOneTick() {
        int currentTicks = world.currentTime().getTicks();
        GameTime newTime = new GameTime(currentTicks + 1);
        world.setTime(newTime);
    }

    /**
     *
     */
    private void advanceTimeOneYear() {
        GameCalendar calendar = (GameCalendar) world.get(WorldItem.Calendar);
        int tpy = calendar.getTicksPerYear();
        int currentTicks = world.currentTime().getTicks();
        GameTime newTime = new GameTime(currentTicks + tpy);
        world.setTime(newTime);
    }

    /**
     *
     */
    public void testProfitsLastYear() {
        assertEquals(0, calc.profitsLastYear(0));
        int currentTicks = world.currentTime().getTicks();
        GameTime newTime = new GameTime(currentTicks + 10);
        world.setTime(newTime);
        assertEquals(0, calc.profitsLastYear(0));

        long income = 100000;
        addIncome(income);
        assertEquals(0, calc.profitsLastYear(0));
        advanceTimeOneYear();
        assertEquals(income, calc.profitsLastYear(0));
    }

    /**
     *
     * @param income
     */
    private void addIncome(long income) {
        CargoBatch batch = new CargoBatch(0, Vec2D.ZERO, 0, 0);
        Transaction transaction = new CargoDeliveryMoneyTransaction(new Money(income), 10, 0, batch, 0);
        FreerailsPrincipal princ = world.getPlayer(0).getPrincipal();
        world.addTransaction(princ, transaction);
    }

    /**
     *
     */
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
