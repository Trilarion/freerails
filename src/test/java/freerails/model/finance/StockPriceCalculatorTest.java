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
package freerails.model.finance;

import freerails.model.MapFixtureFactory2;
import freerails.model.finance.transaction.CargoDeliveryTransaction;
import freerails.model.finance.transaction.Transaction;
import freerails.model.game.Clock;
import freerails.model.game.Time;
import freerails.util.Vec2D;
import freerails.model.world.World;
import freerails.model.cargo.CargoBatch;
import freerails.model.player.Player;
import junit.framework.TestCase;

/**
 *
 */
public class StockPriceCalculatorTest extends TestCase {

    private World world;
    private StockPriceCalculator calculator;

    /**
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        world = MapFixtureFactory2.getCopy();
        calculator = new StockPriceCalculator(world);
    }

    /**
     *
     */
    public void testIsFirstYear() {
        assertTrue(calculator.isFirstYear(0));
        Clock clock = world.getClock();
        Time newTime = new Time(clock.getCurrentTime(), clock.getTicksPerYear() - 1);
        clock.advanceTimeTo(newTime);
        assertTrue(calculator.isFirstYear(0));
        newTime = new Time(clock.getCurrentTime(), 1);
        clock.advanceTimeTo(newTime);
        assertFalse(calculator.isFirstYear(0));
    }

    /**
     *
     */
    public void testNetWorth() {
        Money initialNetworth = new Money(500000);
        assertEquals(initialNetworth, calculator.netWorth(0));
        Clock clock = world.getClock();
        Time newTime = new Time(clock.getCurrentTime(), 1);
        while (clock.getCurrentTime().compareTo(newTime) < 0) {
            clock.advanceTime();
        }
        CargoBatch batch = new CargoBatch(0, Vec2D.ZERO, 0, 0);
        Money income = new Money(100000);
        Transaction transaction = new CargoDeliveryTransaction(income, world.getClock().getCurrentTime(), 10, 0,0, batch);
        Player player = world.getPlayer(0);
        world.addTransaction(player, transaction);
        assertEquals(initialNetworth, calculator.netWorth(0));

        newTime = new Time(clock.getCurrentTime(), clock.getTicksPerYear());
        while (clock.getCurrentTime().compareTo(newTime) < 0) {
            clock.advanceTime();
        }

        Money expectedNetWorth = Money.add(initialNetworth, income);
        assertEquals(expectedNetWorth, calculator.netWorth(0));
    }

    /**
     *
     */
    private void advanceTimeOneTick() {
        Clock clock = world.getClock();

        Time newTime = new Time(clock.getCurrentTime(), 1);
        while (world.getClock().getCurrentTime().compareTo(newTime) < 0) {
            world.getClock().advanceTime();
        }
    }

    /**
     *
     */
    private void advanceTimeOneYear() {
        Clock clock = world.getClock();
        Time newTime = new Time(clock.getCurrentTime(), clock.getTicksPerYear());
        clock.advanceTimeTo(newTime);
    }

    /**
     *
     */
    public void testProfitsLastYear() {
        Clock clock = world.getClock();
        advanceTimeOneYear();
        Time newTime = new Time(clock.getCurrentTime(), 10);
        clock.advanceTimeTo(newTime);
        assertEquals(Money.ZERO, calculator.profitsLastYear(0));

        Money income = new Money(100000);
        addIncome(income);
        assertEquals(Money.ZERO, calculator.profitsLastYear(0));
        advanceTimeOneYear();
        assertEquals(income, calculator.profitsLastYear(0));
    }

    /**
     *
     * @param income
     */
    private void addIncome(Money income) {
        CargoBatch batch = new CargoBatch(0, Vec2D.ZERO, 0, 0);
        Transaction transaction = new CargoDeliveryTransaction(income, world.getClock().getCurrentTime(), 10, 0, 0, batch);
        Player princ = world.getPlayer(0);
        world.addTransaction(princ, transaction);
    }

    /**
     *
     */
    public void testCalculate() {
        Money stockPrice = calculator.calculate()[0].currentPrice;
        assertEquals(new Money(10), stockPrice);
        advanceTimeOneTick();
        addIncome(new Money(100000));
        calculator.calculate();
        stockPrice = calculator.calculate()[0].currentPrice;
        assertEquals(new Money(10), stockPrice);
        advanceTimeOneYear();
        calculator.calculate();
        stockPrice = calculator.calculate()[0].currentPrice;
        assertEquals(new Money(11), stockPrice);
    }
}
