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
package freerails.controller;

import freerails.model.economy.transaction.Transaction;
import freerails.model.economy.transaction.aggregator.FinancialDataAggregator;
import freerails.move.AddPlayerMove;
import freerails.move.Move;
import freerails.move.Status;
import freerails.model.economy.*;
import freerails.model.ModelConstants;
import freerails.model.world.World;
import freerails.model.player.Player;
import freerails.model.simulation.Time;
import freerails.util.WorldGenerator;
import junit.framework.TestCase;

/**
 * Test for FinancialDataGatherer.
 */
public class FinancialDataAggregatorTest extends TestCase {

    private World world;
    private Player player;

    /**
     * Initializes the fixture before each test.
     * @throws Exception if the set up operation fails.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        world = WorldGenerator.minimalWorld();

        player = new Player(0, "Player X");
        // add a new player
        Move move = new AddPlayerMove(player);
        Status status = move.applicable(world);
        assertTrue(status.isSuccess());
        move.apply(world);
    }

    /**
     * Verifies that bond issuance is limited by the resulting interest rate.
     */
    public void testCanIssueBond() {
        FinancialDataAggregator aggregator = new FinancialDataAggregator(world, player);
        aggregator.aggregate();
        assertTrue(aggregator.canIssueBond()); // 5%

        assertTrue(addBond()); // 6%
        assertTrue(addBond()); // 7%
        assertFalse(addBond()); // 8% so can't
        aggregator.aggregate();
        assertEquals(8.0, aggregator.nextBondInterestRate());
    }

    /**
     * Adds a bond and returns true if another bond can be added. Written to
     * avoid copy & paste in testCanIssueBond().
     */
    private boolean addBond() {
        world.addTransaction(player, TransactionUtils.issueBond(5, world.getClock().getCurrentTime()));
        FinancialDataAggregator aggregator = new FinancialDataAggregator(world, player);
        aggregator.aggregate();
        return aggregator.canIssueBond();
    }

    /**
     * Tests next bond interest rate behavior.
     */
    public void testNextBondInterestRate() {
        FinancialDataAggregator financialDataAggregator = new FinancialDataAggregator(world, player);
        financialDataAggregator.aggregate();
        assertEquals(5.0, financialDataAggregator.nextBondInterestRate());
        world.addTransaction(player, TransactionUtils.issueBond(5, world.getClock().getCurrentTime()));
        financialDataAggregator.aggregate();
        assertEquals(6.0, financialDataAggregator.nextBondInterestRate());
    }

    /**
     * Tests treasury stock behavior.
     */
    public void testTreasuryStock() {
        FinancialDataAggregator aggregator = new FinancialDataAggregator(world, player);
        aggregator.aggregate();
        assertEquals(0, aggregator.treasuryStock());

        int treasuryStock = 10000;
        int totalStock = ModelConstants.IPO_SIZE;
        int publicStock = totalStock - treasuryStock;
        Transaction transaction = TransactionUtils.buyOrSellStock(0, treasuryStock, new Money(5), world.getClock().getCurrentTime());
        world.addTransaction(player, transaction);
        aggregator.aggregate();
        assertEquals(treasuryStock, aggregator.treasuryStock());
        assertEquals(totalStock, aggregator.totalShares());
        assertEquals(publicStock, aggregator.sharesHeldByPublic());
    }

    /**
     * Verifies that buying another railroad's stock is recorded as shares held by the player.
     */
    public void testBuyingStakesInOtherRRs() {
        world = WorldGenerator.minimalWorld();
        Player[] players = new Player[2];
        for (int i = 0; i < players.length; i++) {
            players[i] = new Player(i, "Player " + i);
            // add a new player
            Move move = new AddPlayerMove(players[i]);
            Status status = move.applicable(world);
            assertTrue(status.isSuccess());
            move.apply(world);
        }

        // Make player #0 buy stock in player #1
        int quantity = 10000;
        Transaction transaction = TransactionUtils.buyOrSellStock(1, quantity, new Money(5), world.getClock().getCurrentTime());
        world.addTransaction(players[0], transaction);
        FinancialDataAggregator aggregator = new FinancialDataAggregator(world, players[0]);
        aggregator.aggregate();
        assertEquals(0, aggregator.treasuryStock());
        int actual = aggregator.getStockInRRs()[1];
        assertEquals(quantity, actual);
    }

    /**
     * Tests total shares behavior.
     */
    public void testTotalShares() {
        FinancialDataAggregator fdg = new FinancialDataAggregator(world, player);
        int expected = ModelConstants.IPO_SIZE;
        assertEquals(expected, fdg.totalShares());
    }

    /**
     * Verifies repeated aggregation does not accumulate previous share,
     * issuance, or bond totals.
     */
    public void testAggregateResetsShareAndBondState() {
        Time currentTime = world.getClock().getCurrentTime();
        world.addTransaction(player, TransactionUtils.issueStock(-1, 25, Money.ZERO, currentTime));
        world.addTransaction(player, TransactionUtils.buyOrSellStock(
                player.getId(), 300, Money.ZERO, currentTime));
        world.addTransaction(player, TransactionUtils.issueBond(5, currentTime));

        FinancialDataAggregator aggregator = new FinancialDataAggregator(world, player);
        aggregator.aggregate();
        assertEquals(ModelConstants.IPO_SIZE + 25, aggregator.totalShares());
        assertEquals(2, aggregator.getBonds());
        assertEquals(300, aggregator.getStockInRRs()[world.getPlayerIndex(player.getId())]);

        aggregator.aggregate();
        assertEquals(ModelConstants.IPO_SIZE + 25, aggregator.totalShares());
        assertEquals(2, aggregator.getBonds());
        assertEquals(300, aggregator.getStockInRRs()[world.getPlayerIndex(player.getId())]);

        world.addTransaction(player, TransactionUtils.issueStock(-1, 5, Money.ZERO, currentTime));
        world.addTransaction(player, TransactionUtils.buyOrSellStock(
                player.getId(), 50, Money.ZERO, currentTime));
        world.addTransaction(player, TransactionUtils.issueBond(5, currentTime));
        aggregator.aggregate();
        assertEquals(ModelConstants.IPO_SIZE + 30, aggregator.totalShares());
        assertEquals(3, aggregator.getBonds());
        assertEquals(350, aggregator.getStockInRRs()[world.getPlayerIndex(player.getId())]);
    }

    /**
     * Verifies custom time boundaries apply to the share and bond quantities,
     * including derived holdings, and that derived holdings are not cached.
     */
    public void testShareAndBondQuantitiesRespectTimeIntervals() {
        Player holder = new Player(9, "Holder");
        world.addPlayer(holder);

        Time start = new Time(1);
        Time end = new Time(2);
        FinancialDataAggregator aggregator = new FinancialDataAggregator(
                world, player, new Time[]{start, end});
        int holderIndex = world.getPlayerIndex(holder.getId());
        assertEquals(0, aggregator.getStockInThisRRs()[holderIndex]);

        world.getClock().advanceTimeTo(end);
        world.addTransaction(player, TransactionUtils.issueStock(
                -1, 10, Money.ZERO, Time.ZERO));
        world.addTransaction(player, TransactionUtils.issueStock(
                -1, 20, Money.ZERO, start));
        world.addTransaction(player, TransactionUtils.issueStock(
                -1, 30, Money.ZERO, end));
        world.addTransaction(player, TransactionUtils.buyOrSellStock(
                player.getId(), 1, Money.ZERO, Time.ZERO));
        world.addTransaction(player, TransactionUtils.buyOrSellStock(
                player.getId(), 2, Money.ZERO, start));
        world.addTransaction(player, TransactionUtils.buyOrSellStock(
                player.getId(), 3, Money.ZERO, end));
        world.addTransaction(player, TransactionUtils.issueBond(5, Time.ZERO));
        world.addTransaction(player, TransactionUtils.issueBond(5, start));
        world.addTransaction(player, TransactionUtils.issueBond(5, end));
        world.addTransaction(holder, TransactionUtils.buyOrSellStock(
                player.getId(), 10, Money.ZERO, Time.ZERO));
        world.addTransaction(holder, TransactionUtils.buyOrSellStock(
                player.getId(), 20, Money.ZERO, start));
        world.addTransaction(holder, TransactionUtils.buyOrSellStock(
                player.getId(), 30, Money.ZERO, end));

        aggregator.aggregate();
        assertEquals(ModelConstants.IPO_SIZE + 20, aggregator.totalShares());
        assertEquals(1, aggregator.getBonds());
        assertEquals(2, aggregator.getStockInRRs()[world.getPlayerIndex(player.getId())]);
        int[] stockInThisRRs = aggregator.getStockInThisRRs();
        assertEquals(2, stockInThisRRs[world.getPlayerIndex(player.getId())]);
        assertEquals(20, stockInThisRRs[holderIndex]);
        assertEquals(ModelConstants.IPO_SIZE - 2, aggregator.sharesHeldByPublic());

        world.addTransaction(holder, TransactionUtils.buyOrSellStock(
                player.getId(), 5, Money.ZERO, start));
        assertEquals(25, aggregator.getStockInThisRRs()[holderIndex]);
    }
}