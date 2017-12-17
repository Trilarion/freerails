/*
 * Created on 04-Oct-2004
 *
 */
package freerails.controller;

import freerails.world.accounts.*;
import freerails.world.common.GameTime;
import freerails.world.common.Money;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.player.Player;
import freerails.world.top.ITEM;
import freerails.world.top.ReadOnlyWorld;
import freerails.world.top.TransactionAggregator;

/**
 * Gathers the financial data for a company.
 *
 * @author Luke
 * @author smackay
 */
public class FinancialDataGatherer extends TransactionAggregator {
    private int totalShares = 100000;

    private final int playerID;

    private int bonds;

    private final int[] stockInRRs;

    private int[] stockInThisRRs;

    @Override
    protected void incrementRunningTotal(int transactionID) {
        Transaction t = super.w.getTransaction(super.principal, transactionID);

        if (t instanceof AddItemTransaction) {
            AddItemTransaction ait = (AddItemTransaction) t;

            if (t instanceof StockTransaction
                    && ait.getCategory() == Transaction.Category.ISSUE_STOCK
                    && ait.getType() == -1) {
                // If it is a change in the total number of shares issued.
                StockTransaction ist = (StockTransaction) t;
                totalShares += ist.getQuantity();

            } else if (t instanceof StockTransaction
                    && ait.getCategory() == Transaction.Category.TRANSFER_STOCK) {
                //
                stockInRRs[ait.getType()] += ait.getQuantity();

            } else if (t instanceof BondTransaction) {
                bonds += ait.getQuantity();
            }
        } else {
            super.incrementRunningTotal(transactionID);
        }
    }

    @Override
    protected void setTotalsArrayLength(int length) {
        // TODO Auto-generated method stub
        super.setTotalsArrayLength(length);
    }

    @Override
    protected void storeRunningTotal(int timeIndex) {
        // TODO Auto-generated method stub
        super.storeRunningTotal(timeIndex);
    }

    public FinancialDataGatherer(ReadOnlyWorld w, FreerailsPrincipal principal) {
        super(w, principal);
        stockInRRs = new int[w.getNumberOfPlayers()];
        calculateValues();
        this.playerID = w.getID(principal);
    }

    public void changeTreasuryStock(int deltaStock) {
    }

    public void changeStake(int stakeHolder, int deltaStock) {
    }

    public boolean canIssueBond() {
        return nextBondInterestRate() <= 7;
    }

    public boolean canBuyStock() {
        return totalShares > 0;
    }

    public int nextBondInterestRate() {
        EconomicClimate ec = (EconomicClimate) w.get(ITEM.ECONOMIC_CLIMATE);
        return bonds + ec.getBaseInterestRate();
    }

    public int[] bondInterestRates() {
        return null;
    }

    /**
     * Returns the number of stock in the Treasury
     */
    public int treasuryStock() {
        return stockInRRs[playerID];
    }

    /**
     * Returns The number of open Shares
     */
    public int totalShares() {
        return totalShares;
    }

    public int sharesHeldByPublic() {
        int[] stock = getStockInThisRRs();
        int returnValue = this.totalShares;
        for (int aStock : stock) {
            returnValue -= aStock;
        }
        return returnValue;
    }

    public boolean thisRRHasStakeIn(int otherReId) {
        return stockInRRs[otherReId] > 0;
    }

    public Money netWorth() {
        NetWorthCalculator nwc = new NetWorthCalculator(w, principal);
        GameTime[] times = {GameTime.BIG_BANG, GameTime.END_OF_THE_WORLD};
        nwc.setTimes(times);
        return nwc.calculateValue();
    }

    @Override
    protected boolean condition(int transactionID) {
        // We'll do the work when incrementRunningTotal gets called.
        return true;
    }

    public int[] getStockInThisRRs() {
        if (null == stockInThisRRs) {
            stockInThisRRs = new int[w.getNumberOfPlayers()];
            for (int i = 0; i < w.getNumberOfPlayers(); i++) {
                Player p = w.getPlayer(i);
                FinancialDataGatherer temp = new FinancialDataGatherer(w, p
                        .getPrincipal());
                stockInThisRRs[i] = temp.stockInRRs[this.playerID];
            }
        }

        return stockInThisRRs;
    }

    public int[] getStockInRRs() {
        return stockInRRs;
    }

    public int getBonds() {
        return bonds;
    }
}
