/*
 * Created on 04-Oct-2004
 *
 */
package jfreerails.controller;

import jfreerails.world.accounts.AddItemTransaction;
import jfreerails.world.accounts.BondTransaction;
import jfreerails.world.accounts.IssueStockTransaction;
import jfreerails.world.accounts.Transaction;
import jfreerails.world.common.Money;
import jfreerails.world.player.Player;
import jfreerails.world.top.MapFixtureFactory;
import jfreerails.world.top.World;
import jfreerails.world.top.WorldImpl;
import junit.framework.TestCase;


/**
 * JUnit test for FinancialDataGatherer.
 * @author Luke
 *
 */
public class FinancialDataGathererTest extends TestCase {
    World w;
    Player player = MapFixtureFactory.TEST_PLAYER;

    protected void setUp() throws Exception {
        w = new WorldImpl();

        w.addPlayer(player);
        w.addTransaction(BondTransaction.issueBond(5), player.getPrincipal());
        w.addTransaction(IssueStockTransaction.issueStock(
                FinancialMoveProducer.IPO_SIZE, 5), player.getPrincipal());
    }

    public void testChangeTreasuryStock() {
    }

    public void testChangeStake() {
    }

    public void testCanIssueBond() {
        FinancialDataGatherer fdg = new FinancialDataGatherer(w,
                player.getPrincipal());
        assertTrue(fdg.canIssueBond()); //5%

        assertTrue(addBond()); //6%
        assertTrue(addBond()); //7%		
        assertFalse(addBond()); //8% so can't	
        fdg = new FinancialDataGatherer(w, player.getPrincipal());
        assertEquals(8, fdg.nextBondInterestRate());
    }

    /** Adds a bond and returns true if another bond can be added. Written to
     * avoid copy & paste in testCanIssueBond().*/
    private boolean addBond() {
        FinancialDataGatherer fdg;
        w.addTransaction(BondTransaction.issueBond(5), player.getPrincipal());
        fdg = new FinancialDataGatherer(w, player.getPrincipal());

        boolean canIssueBond = fdg.canIssueBond();

        return canIssueBond;
    }

    public void testNextBondInterestRate() {
        FinancialDataGatherer fdg = new FinancialDataGatherer(w,
                player.getPrincipal());
        assertEquals(5, fdg.nextBondInterestRate());
        w.addTransaction(BondTransaction.issueBond(5), player.getPrincipal());
        fdg = new FinancialDataGatherer(w, player.getPrincipal());
        assertEquals(6, fdg.nextBondInterestRate());
    }

    public void testBondInterestRates() {
    }

    public void testTreasuryStock() {
        FinancialDataGatherer fdg = new FinancialDataGatherer(w,
                player.getPrincipal());
        assertEquals(0, fdg.treasuryStock());

        Transaction t = new AddItemTransaction(Transaction.STOCK, 0,
                FinancialMoveProducer.SHARE_BUNDLE_SIZE, new Money(0));
        w.addTransaction(t, player.getPrincipal());
        fdg = new FinancialDataGatherer(w, player.getPrincipal());
        assertEquals(FinancialMoveProducer.SHARE_BUNDLE_SIZE,
            fdg.treasuryStock());
    }

    public void testTotalShares() {
        FinancialDataGatherer fdg = new FinancialDataGatherer(w,
                player.getPrincipal());
        int expected = FinancialMoveProducer.IPO_SIZE;
        assertEquals(expected, fdg.totalShares());
    }

    public void testOtherRRShares() {
    }

    public void testOtherRRsWithStake() {
    }

    public void testShareHolderEquity() {
    }

    public void testSharePrice() {
    }

    public void testProfitLastYear() {
    }

    public void testNetWorth() {
    }
}