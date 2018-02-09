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

import freerails.world.ITEM;
import freerails.world.ReadOnlyWorld;
import freerails.world.TransactionAggregator;
import freerails.world.finances.*;
import freerails.world.game.GameTime;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.player.Player;

/**
 * Gathers the financial data for a company.
 */
public class FinancialDataGatherer extends TransactionAggregator {

    private final int playerID;
    private final int[] stockInRRs;
    private int totalShares = 100000;
    private int bonds;
    private int[] stockInThisRRs;

    /**
     * @param w
     * @param principal
     */
    public FinancialDataGatherer(ReadOnlyWorld w, FreerailsPrincipal principal) {
        super(w, principal);
        stockInRRs = new int[w.getNumberOfPlayers()];
        calculateValues();
        playerID = w.getID(principal);
    }

    /**
     * @param transactionID
     */
    @Override
    protected void incrementRunningTotal(int transactionID) {
        Transaction transaction = super.world.getTransaction(super.principal, transactionID);

        if (transaction instanceof ItemTransaction) {
            ItemTransaction ait = (ItemTransaction) transaction;

            if (transaction instanceof StockItemTransaction && ait.getCategory() == TransactionCategory.ISSUE_STOCK && ait.getType() == -1) {
                // If it is a change in the total number of shares issued.
                StockItemTransaction ist = (StockItemTransaction) transaction;
                totalShares += ist.getQuantity();
            } else if (transaction instanceof StockItemTransaction && ait.getCategory() == TransactionCategory.TRANSFER_STOCK) {
                //
                stockInRRs[ait.getType()] += ait.getQuantity();
            } else if (transaction instanceof BondItemTransaction) {
                bonds += ait.getQuantity();
            }
        } else {
            super.incrementRunningTotal(transactionID);
        }
    }

    /**
     * @return
     */
    public boolean canIssueBond() {
        return nextBondInterestRate() <= 7;
    }

    /**
     * @return
     */
    public double nextBondInterestRate() {
        EconomicClimate ec = (EconomicClimate) world.get(ITEM.ECONOMIC_CLIMATE);
        return bonds + ec.getBaseInterestRate();
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

    /**
     * @return
     */
    public int sharesHeldByPublic() {
        int[] stock = getStockInThisRRs();
        int returnValue = totalShares;
        for (int aStock : stock) {
            returnValue -= aStock;
        }
        return returnValue;
    }

    /**
     * @return
     */
    public Money netWorth() {
        NetWorthCalculator nwc = new NetWorthCalculator(world, principal);
        GameTime[] times = {GameTime.BIG_BANG, GameTime.DOOMSDAY};
        nwc.setTimes(times);
        return nwc.calculateValue();
    }

    @Override
    protected boolean condition(int transactionID) {
        // We'll do the work when incrementRunningTotal gets called.
        return true;
    }

    /**
     * @return
     */
    public int[] getStockInThisRRs() {
        if (null == stockInThisRRs) {
            stockInThisRRs = new int[world.getNumberOfPlayers()];
            for (int i = 0; i < world.getNumberOfPlayers(); i++) {
                Player player = world.getPlayer(i);
                FinancialDataGatherer temp = new FinancialDataGatherer(world, player.getPrincipal());
                stockInThisRRs[i] = temp.stockInRRs[playerID];
            }
        }

        return stockInThisRRs;
    }

    /**
     * @return
     */
    public int[] getStockInRRs() {
        return stockInRRs;
    }

    /**
     * @return
     */
    public int getBonds() {
        return bonds;
    }
}
