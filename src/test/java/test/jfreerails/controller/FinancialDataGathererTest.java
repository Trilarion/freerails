/*
 * Created on 04-Oct-2004
 *
 */
package jfreerails.controller;

import jfreerails.move.AddPlayerMove;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.world.accounts.BondTransaction;
import jfreerails.world.accounts.StockTransaction;
import jfreerails.world.accounts.Transaction;
import jfreerails.world.common.Money;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.player.Player;
import jfreerails.world.top.World;
import jfreerails.world.top.WorldImpl;
import junit.framework.TestCase;

/**
 * JUnit test for FinancialDataGatherer.
 * 
 * @author Luke
 * 
 */
public class FinancialDataGathererTest extends TestCase {
    World w;

    Player player;

    @Override
    protected void setUp() throws Exception {
        player = new Player("Player X", 0);
        w = new WorldImpl();

        Move addPlayer = AddPlayerMove.generateMove(w, player);
        MoveStatus ms = addPlayer.doMove(w, Player.AUTHORITATIVE);
        assertTrue(ms.ok);
    }

    public void testCanIssueBond() {
        FinancialDataGatherer fdg = new FinancialDataGatherer(w, player
                .getPrincipal());
        assertTrue(fdg.canIssueBond()); // 5%

        assertTrue(addBond()); // 6%
        assertTrue(addBond()); // 7%
        assertFalse(addBond()); // 8% so can't
        fdg = new FinancialDataGatherer(w, player.getPrincipal());
        assertEquals(8, fdg.nextBondInterestRate());
    }

    /**
     * Adds a bond and returns true if another bond can be added. Written to
     * avoid copy & paste in testCanIssueBond().
     */
    private boolean addBond() {
        FinancialDataGatherer fdg;
        w.addTransaction(player.getPrincipal(), BondTransaction.issueBond(5));
        fdg = new FinancialDataGatherer(w, player.getPrincipal());

        boolean canIssueBond = fdg.canIssueBond();

        return canIssueBond;
    }

    public void testNextBondInterestRate() {
        FinancialDataGatherer fdg = new FinancialDataGatherer(w, player
                .getPrincipal());
        assertEquals(5, fdg.nextBondInterestRate());
        w.addTransaction(player.getPrincipal(), BondTransaction.issueBond(5));
        fdg = new FinancialDataGatherer(w, player.getPrincipal());
        assertEquals(6, fdg.nextBondInterestRate());
    }

    public void testTreasuryStock() {
        FreerailsPrincipal principal = player.getPrincipal();
        FinancialDataGatherer fdg = new FinancialDataGatherer(w, principal);
        assertEquals(0, fdg.treasuryStock());

        int treasuryStock = 10000;
        int totalStock = FinancialMoveProducer.IPO_SIZE;
        int publicStock = totalStock - treasuryStock;
        Transaction t = StockTransaction.buyOrSellStock(0, treasuryStock,
                new Money(5));
        w.addTransaction(principal, t);
        fdg = new FinancialDataGatherer(w, principal);
        assertEquals(treasuryStock, fdg.treasuryStock());
        assertEquals(totalStock, fdg.totalShares());
        assertEquals(publicStock, fdg.sharesHeldByPublic());
    }

    public void testBuyingStakesInOtherRRs() {
        w = new WorldImpl();
        Player[] players = new Player[2];
        for (int i = 0; i < players.length; i++) {
            players[i] = new Player("Player " + i, i);
            Move addPlayer = AddPlayerMove.generateMove(w, players[i]);
            MoveStatus ms = addPlayer.doMove(w, Player.AUTHORITATIVE);
            assertTrue(ms.ok);
        }

        // Make player #0 buy stock in player #1
        int quantity = 10000;
        Transaction t = StockTransaction.buyOrSellStock(1, quantity, new Money(
                5));
        w.addTransaction(players[0].getPrincipal(), t);
        FinancialDataGatherer fdg = new FinancialDataGatherer(w, players[0]
                .getPrincipal());
        assertEquals(0, fdg.treasuryStock());
        int acutal = fdg.getStockInRRs()[1];
        assertEquals(quantity, acutal);
    }

    public void testTotalShares() {
        FinancialDataGatherer fdg = new FinancialDataGatherer(w, player
                .getPrincipal());
        int expected = FinancialMoveProducer.IPO_SIZE;
        assertEquals(expected, fdg.totalShares());
    }

}