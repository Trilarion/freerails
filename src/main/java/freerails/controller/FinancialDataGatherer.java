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
 */
public class FinancialDataGatherer extends TransactionAggregator {
    private final int playerID;
    private final int[] stockInRRs;
    private int totalShares = 100000;
    private int bonds;
    private int[] stockInThisRRs;

    /**
     *
     * @param w
     * @param principal
     */
    public FinancialDataGatherer(ReadOnlyWorld w, FreerailsPrincipal principal) {
        super(w, principal);
        stockInRRs = new int[w.getNumberOfPlayers()];
        calculateValues();
        this.playerID = w.getID(principal);
    }

    /**
     *
     * @param transactionID
     */
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

    /**
     *
     * @param deltaStock
     */
    public void changeTreasuryStock(int deltaStock) {
    }

    /**
     *
     * @param stakeHolder
     * @param deltaStock
     */
    public void changeStake(int stakeHolder, int deltaStock) {
    }

    /**
     *
     * @return
     */
    public boolean canIssueBond() {
        return nextBondInterestRate() <= 7;
    }

    /**
     *
     * @return
     */
    public boolean canBuyStock() {
        return totalShares > 0;
    }

    /**
     *
     * @return
     */
    public int nextBondInterestRate() {
        EconomicClimate ec = (EconomicClimate) w.get(ITEM.ECONOMIC_CLIMATE);
        return bonds + ec.getBaseInterestRate();
    }

    /**
     *
     * @return
     */
    public int[] bondInterestRates() {
        return null;
    }

    /**
     * Returns the number of stock in the Treasury
     * @return 
     */
    public int treasuryStock() {
        return stockInRRs[playerID];
    }

    /**
     * Returns The number of open Shares
     * @return 
     */
    public int totalShares() {
        return totalShares;
    }

    /**
     *
     * @return
     */
    public int sharesHeldByPublic() {
        int[] stock = getStockInThisRRs();
        int returnValue = this.totalShares;
        for (int aStock : stock) {
            returnValue -= aStock;
        }
        return returnValue;
    }

    /**
     *
     * @param otherReId
     * @return
     */
    public boolean thisRRHasStakeIn(int otherReId) {
        return stockInRRs[otherReId] > 0;
    }

    /**
     *
     * @return
     */
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

    /**
     *
     * @return
     */
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

    /**
     *
     * @return
     */
    public int[] getStockInRRs() {
        return stockInRRs;
    }

    /**
     *
     * @return
     */
    public int getBonds() {
        return bonds;
    }
}
