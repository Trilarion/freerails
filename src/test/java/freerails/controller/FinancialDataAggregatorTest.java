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
package freerails.controller;

import freerails.model.finance.transaction.Transaction;
import freerails.model.finance.transaction.aggregator.FinancialDataAggregator;
import freerails.move.AddPlayerMove;
import freerails.move.Move;
import freerails.nove.Status;
import freerails.model.finance.*;
import freerails.model.ModelConstants;
import freerails.model.world.World;
import freerails.model.player.Player;
import freerails.util.WorldGenerator;
import junit.framework.TestCase;

/**
 * Test for FinancialDataGatherer.
 */
public class FinancialDataAggregatorTest extends TestCase {

    private World world;
    private Player player;

    /**
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        world = WorldGenerator.minimalWorld();

        player = new Player(0, "Player X");
        Move addPlayer = AddPlayerMove.generateMove(world, player);
        Status status = addPlayer.doMove(world, Player.AUTHORITATIVE);
        assertTrue(status.isSuccess());
    }

    /**
     *
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
     *
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
     *
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
     *
     */
    public void testBuyingStakesInOtherRRs() {
        world = WorldGenerator.minimalWorld();
        Player[] players = new Player[2];
        for (int i = 0; i < players.length; i++) {
            players[i] = new Player(i, "Player " + i);
            Move addPlayer = AddPlayerMove.generateMove(world, players[i]);
            Status status = addPlayer.doMove(world, Player.AUTHORITATIVE);
            assertTrue(status.isSuccess());
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
     *
     */
    public void testTotalShares() {
        FinancialDataAggregator fdg = new FinancialDataAggregator(world, player);
        int expected = ModelConstants.IPO_SIZE;
        assertEquals(expected, fdg.totalShares());
    }
}