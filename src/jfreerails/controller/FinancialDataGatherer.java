/*
 * Created on 04-Oct-2004
 *
 */
package jfreerails.controller;

import jfreerails.world.accounts.AddItemTransaction;
import jfreerails.world.accounts.BondTransaction;
import jfreerails.world.accounts.EconomicClimate;
import jfreerails.world.accounts.IssueStockTransaction;
import jfreerails.world.accounts.Transaction;
import jfreerails.world.common.Money;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.player.Player;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.TransactionAggregator;


/**
 * Gathers the financial data for a company.
 *
 * @author Luke
 *
 */
public class FinancialDataGatherer extends TransactionAggregator {
    private int totalShares;
    private int treasuryStock;
    private final int playerID;
    private int bonds;

    protected void incrementRunningTotal(int transactionID) {
        Transaction t = super.w.getTransaction(transactionID, super.principal);

        if (t instanceof AddItemTransaction) {
            AddItemTransaction ait = (AddItemTransaction)t;

            if (t instanceof IssueStockTransaction) {
                IssueStockTransaction ist = (IssueStockTransaction)t;
                totalShares += ist.getQuantity();
            } else if (t instanceof BondTransaction) {
                bonds += ait.getQuantity();
            } else {
                //If it is a change in treasury stock.
                if (ait.getCategory() == Transaction.STOCK &&
                        ait.getType() == playerID) {
                    treasuryStock += ait.getQuantity();
                }
            }
        } else {
            super.incrementRunningTotal(transactionID);
        }
    }

    protected void setTotalsArrayLength(int length) {
        // TODO Auto-generated method stub
        super.setTotalsArrayLength(length);
    }

    protected void storeRunningTotal(int timeIndex) {
        // TODO Auto-generated method stub
        super.storeRunningTotal(timeIndex);
    }

    public FinancialDataGatherer(ReadOnlyWorld w, FreerailsPrincipal principal) {
        super(w, principal);
        calculateValues();

        int id = 0;

        for (int i = 0; i < w.getNumberOfPlayers(); i++) {
            Player player = w.getPlayer(i);

            if (principal.equals(player.getPrincipal())) {
                id = i;

                break;
            }
        }

        this.playerID = id;
    }

    public void changeTreasuryStock(int deltaStock) {
    }

    public void changeStake(int stakeHolder, int deltaStock) {
    }

    public boolean canIssueBond() {
        return nextBondInterestRate() <= 7;
    }

    public int nextBondInterestRate() {
        EconomicClimate ec = (EconomicClimate)w.get(ITEM.ECONOMIC_CLIMATE);

        return bonds + ec.getBaseInterestRate();
    }

    public int[] bondInterestRates() {
        return null;
    }

    public int treasuryStock() {
        return treasuryStock;
    }

    public int totalShares() {
        return totalShares;
    }

    public int otherRRShares() {
        return 0;
    }

    /** Returns the stakes that other players have in this company.*/
    public int[] otherRRsWithStake() {
        return null;
    }

    public Money shareHolderEquity() {
        return null;
    }

    public Money sharePrice() {
        return null;
    }

    public Money profitLastYear() {
        return null;
    }

    public Money netWorth() {
        return null;
    }

    protected boolean condition(int transactionID) {
        //We'll do the work when incrementRunningTotal gets called.
        return true;
    }
}