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

/*
 *
 */
package freerails.model.economy;

import freerails.model.MapFixtureFactory2;
import freerails.model.economy.transaction.CargoDeliveryTransaction;
import freerails.model.economy.transaction.ItemTransaction;
import freerails.model.economy.transaction.Transaction;
import freerails.model.economy.transaction.TransactionCategory;
import freerails.model.economy.transaction.aggregator.FinancialDataAggregator;
import freerails.model.simulation.Clock;
import freerails.model.simulation.Time;
import freerails.util.IntPoint2D;
import freerails.util.WorldGenerator;
import freerails.model.world.World;
import freerails.model.operations.cargo.CargoBatch;
import freerails.model.player.Player;
import junit.framework.TestCase;

/**
 * Tests stock price calculator behavior.
 */
public class StockPriceCalculatorTest extends TestCase {

    private World world;
    private StockPriceCalculator calculator;

    /**
     * Initializes the fixture before each test.
     * @throws Exception if the set up operation fails.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        world = MapFixtureFactory2.getCopy();
        calculator = new StockPriceCalculator(world);
    }

    /**
     * Verifies that the first-year check is true initially and changes after the first year elapses.
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
     * Tests net worth behavior.
     */
    public void testNetWorth() {
        Money initialNetworth = new Money(500000);
        assertEquals(initialNetworth, calculator.netWorth(0));
        Clock clock = world.getClock();
        Time newTime = new Time(clock.getCurrentTime(), 1);
        while (clock.getCurrentTime().compareTo(newTime) < 0) {
            clock.advanceTime();
        }
        CargoBatch batch = new CargoBatch(0, IntPoint2D.ZERO, 0, 0);
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
     * Performs advance time one tick.
     */
    private void advanceTimeOneTick() {
        Clock clock = world.getClock();

        Time newTime = new Time(clock.getCurrentTime(), 1);
        while (world.getClock().getCurrentTime().compareTo(newTime) < 0) {
            world.getClock().advanceTime();
        }
    }

    /**
     * Performs advance time one year.
     */
    private void advanceTimeOneYear() {
        Clock clock = world.getClock();
        Time newTime = new Time(clock.getCurrentTime(), clock.getTicksPerYear());
        clock.advanceTimeTo(newTime);
    }

    /**
     * Tests profits last year behavior.
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
     * Adds income.
     * @param income the income to use in the test.
     */
    private void addIncome(Money income) {
        CargoBatch batch = new CargoBatch(0, IntPoint2D.ZERO, 0, 0);
        Transaction transaction = new CargoDeliveryTransaction(income, world.getClock().getCurrentTime(), 10, 0, 0, batch);
        Player princ = world.getPlayer(0);
        world.addTransaction(princ, transaction);
    }

    /**
     * Tests calculate behavior.
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

    /**
     * Tests sparse player ids in stock transactions behavior.
     */
    public void testSparsePlayerIdsInStockTransactions() {
        World sparseWorld = WorldGenerator.minimalWorld();
        Player holder = new Player(40, "Holder");
        Player company = new Player(7, "Company");
        sparseWorld.addPlayer(holder);
        sparseWorld.addPlayer(company);
        sparseWorld.addTransaction(holder, new ItemTransaction(
                TransactionCategory.TRANSFER_STOCK, Money.ZERO,
                sparseWorld.getClock().getCurrentTime(), 500, company.getId()));

        FinancialDataAggregator aggregator = new FinancialDataAggregator(sparseWorld, holder);
        aggregator.aggregate();

        assertEquals(500, aggregator.getStockInRRs()[sparseWorld.getPlayerIndex(company.getId())]);
    }
}
