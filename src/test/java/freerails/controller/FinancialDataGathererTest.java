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

import freerails.move.AddPlayerMove;
import freerails.move.Move;
import freerails.move.MoveStatus;
import freerails.world.World;
import freerails.world.WorldConstants;
import freerails.world.WorldImpl;
import freerails.world.finances.BondItemTransaction;
import freerails.world.finances.Money;
import freerails.world.finances.StockItemTransaction;
import freerails.world.finances.Transaction;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.player.Player;
import junit.framework.TestCase;

/**
 * Test for FinancialDataGatherer.
 */
public class FinancialDataGathererTest extends TestCase {

    private World world;
    private Player player;

    /**
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        player = new Player("Player X", 0);
        world = new WorldImpl();

        Move addPlayer = AddPlayerMove.generateMove(world, player);
        MoveStatus ms = addPlayer.doMove(world, Player.AUTHORITATIVE);
        assertTrue(ms.status);
    }

    /**
     *
     */
    public void testCanIssueBond() {
        FinancialDataGatherer fdg = new FinancialDataGatherer(world, player
                .getPrincipal());
        assertTrue(fdg.canIssueBond()); // 5%

        assertTrue(addBond()); // 6%
        assertTrue(addBond()); // 7%
        assertFalse(addBond()); // 8% so can't
        fdg = new FinancialDataGatherer(world, player.getPrincipal());
        assertEquals(8.0, fdg.nextBondInterestRate());
    }

    /**
     * Adds a bond and returns true if another bond can be added. Written to
     * avoid copy & paste in testCanIssueBond().
     */
    private boolean addBond() {
        FinancialDataGatherer fdg;
        world.addTransaction(player.getPrincipal(), BondItemTransaction.issueBond(5));
        fdg = new FinancialDataGatherer(world, player.getPrincipal());

        return fdg.canIssueBond();
    }

    /**
     *
     */
    public void testNextBondInterestRate() {
        FinancialDataGatherer fdg = new FinancialDataGatherer(world, player
                .getPrincipal());
        assertEquals(5.0, fdg.nextBondInterestRate());
        world.addTransaction(player.getPrincipal(), BondItemTransaction.issueBond(5));
        fdg = new FinancialDataGatherer(world, player.getPrincipal());
        assertEquals(6.0, fdg.nextBondInterestRate());
    }

    /**
     *
     */
    public void testTreasuryStock() {
        FreerailsPrincipal principal = player.getPrincipal();
        FinancialDataGatherer fdg = new FinancialDataGatherer(world, principal);
        assertEquals(0, fdg.treasuryStock());

        int treasuryStock = 10000;
        int totalStock = WorldConstants.IPO_SIZE;
        int publicStock = totalStock - treasuryStock;
        Transaction t = StockItemTransaction.buyOrSellStock(0, treasuryStock,
                new Money(5));
        world.addTransaction(principal, t);
        fdg = new FinancialDataGatherer(world, principal);
        assertEquals(treasuryStock, fdg.treasuryStock());
        assertEquals(totalStock, fdg.totalShares());
        assertEquals(publicStock, fdg.sharesHeldByPublic());
    }

    /**
     *
     */
    public void testBuyingStakesInOtherRRs() {
        world = new WorldImpl();
        Player[] players = new Player[2];
        for (int i = 0; i < players.length; i++) {
            players[i] = new Player("Player " + i, i);
            Move addPlayer = AddPlayerMove.generateMove(world, players[i]);
            MoveStatus ms = addPlayer.doMove(world, Player.AUTHORITATIVE);
            assertTrue(ms.status);
        }

        // Make player #0 buy stock in player #1
        int quantity = 10000;
        Transaction t = StockItemTransaction.buyOrSellStock(1, quantity, new Money(
                5));
        world.addTransaction(players[0].getPrincipal(), t);
        FinancialDataGatherer fdg = new FinancialDataGatherer(world, players[0]
                .getPrincipal());
        assertEquals(0, fdg.treasuryStock());
        int acutal = fdg.getStockInRRs()[1];
        assertEquals(quantity, acutal);
    }

    /**
     *
     */
    public void testTotalShares() {
        FinancialDataGatherer fdg = new FinancialDataGatherer(world, player
                .getPrincipal());
        int expected = WorldConstants.IPO_SIZE;
        assertEquals(expected, fdg.totalShares());
    }

}