/*
 * Created on 04-Oct-2004
 *
 */
package jfreerails.controller;

import java.util.HashMap;
import jfreerails.world.accounts.AddItemTransaction;
import jfreerails.world.accounts.BondTransaction;
import jfreerails.world.accounts.EconomicClimate;
import jfreerails.world.accounts.IssueStockTransaction;
import jfreerails.world.accounts.Transaction;
import jfreerails.world.common.Money;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.TransactionAggregator;


/**
 * Gathers the financial data for a company.
 *
 * @author Luke
 * @author smackay
 */
public class FinancialDataGatherer extends TransactionAggregator {
    private int totalShares = 100000;
    private int treasuryStock;
    private final int playerID;
    private int bonds;
    private HashMap<Integer, Integer> otherRRSharesHashMap = new HashMap<Integer, Integer>();
    private HashMap<Integer, Integer> otherRRsWithStakeHashMap = new HashMap<Integer, Integer>();
    

    protected void incrementRunningTotal(int transactionID) {
        Transaction t = super.w.getTransaction(transactionID, super.principal);

        if (t instanceof AddItemTransaction) {
            AddItemTransaction ait = (AddItemTransaction)t;

            if (t instanceof IssueStockTransaction && ait.getCategory() == Transaction.Category.ISSUE_STOCK &&
                        ait.getType() == playerID ) {
                //If it is a change in treasury stock.
                treasuryStock += ait.getQuantity();
                IssueStockTransaction ist = (IssueStockTransaction)t;
                totalShares -= ist.getQuantity();
                
            }   else if (t instanceof IssueStockTransaction && ait.getCategory() == Transaction.Category.SELL_STOCK &&
                        ait.getType() == playerID ){
                 //If it is a change in treasury stock.
                treasuryStock -= ait.getQuantity();
                IssueStockTransaction ist = (IssueStockTransaction)t;
                totalShares += ist.getQuantity();
                
            }   else if (t instanceof IssueStockTransaction && ait.getCategory() == Transaction.Category.BUY_PLAYER_STOCK){
                // When another Player buys stock of this player
                IssueStockTransaction ist = (IssueStockTransaction)t;
                totalShares -= ist.getQuantity();
                 int playerId = ist.getType();
                if(otherRRsWithStakeHashMap.containsKey(playerId)){
                    Integer totalOwnedShares = otherRRsWithStakeHashMap.get(playerId);
                    otherRRsWithStakeHashMap.remove(playerId);
                    otherRRsWithStakeHashMap.put(playerId, (ist.getQuantity() + totalOwnedShares.intValue()));
                } else {
                    int temp = ist.getQuantity();
                    otherRRsWithStakeHashMap.put(playerId, temp);
                }
            }   else if (t instanceof IssueStockTransaction && ait.getCategory() == Transaction.Category.SELL_PLAYER_STOCK){
                // When another Player sells stock of this player
                IssueStockTransaction ist = (IssueStockTransaction)t;
                totalShares += ist.getQuantity();
                int playerId = ist.getType();
                Integer totalOwnedShares = otherRRsWithStakeHashMap.get(playerId);
                otherRRsWithStakeHashMap.remove(playerId);
                if((totalOwnedShares.intValue() - ist.getQuantity()) > 0){
                    otherRRsWithStakeHashMap.put(playerId, (ist.getQuantity() - totalOwnedShares.intValue()));
                }
                
            }   else if (t instanceof IssueStockTransaction && ait.getCategory() == Transaction.Category.ISSUE_STOCK) {
                //When this player buys stock from another Player
                IssueStockTransaction ist = (IssueStockTransaction)t;
                int playerId = ist.getType();
                if(otherRRSharesHashMap.containsKey(playerId)){
                    Integer totalOwnedShares = otherRRSharesHashMap.get(playerId);
                    otherRRSharesHashMap.remove(playerId);
                    otherRRSharesHashMap.put(playerId, (ist.getQuantity() + totalOwnedShares.intValue()));
                } else {
                    int temp = ist.getQuantity();
                    otherRRSharesHashMap.put(playerId, temp);
                }
            }   else if (t instanceof IssueStockTransaction && ait.getCategory() == Transaction.Category.SELL_STOCK) {
                //When this player sells stock from another Player
                IssueStockTransaction ist = (IssueStockTransaction)t;
                int playerId = ist.getType();
                Integer totalOwnedShares = otherRRSharesHashMap.get(playerId);
                otherRRSharesHashMap.remove(playerId);
                if(totalOwnedShares != null && (totalOwnedShares.intValue() - ist.getQuantity()) > 0){
                    otherRRSharesHashMap.put(playerId, (ist.getQuantity() - totalOwnedShares.intValue()));
                }
                
            } else if (t instanceof BondTransaction) {
                bonds += ait.getQuantity();
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
        EconomicClimate ec = (EconomicClimate)w.get(ITEM.ECONOMIC_CLIMATE);
        return bonds + ec.getBaseInterestRate();
    }

    public int[] bondInterestRates() {
        return null;
    }
    
    /** Returns the number of stock in the Treasury*/
    public int treasuryStock() {
        return treasuryStock;
    }
    /** Returns The number of open Shares*/
    public int totalShares() {
        return totalShares;
    }
    /** Returns the stakes that The player has in other players*/
    public HashMap<Integer, Integer> otherRRShares() {
        return otherRRSharesHashMap;
    }

    /** Returns the stakes that other players have in this company.*/
    public HashMap otherRRsWithStake() {
        return otherRRsWithStakeHashMap;
    }

    public Money shareHolderEquity() {
        return null;
    }

    public Money sharePrice() {
        // TODO: Find out the True Share Price.
        return new Money(5);
    }

    public Money profitLastYear() {
        return null;
    }

    public Money netWorth() {
        //TODO: find out the True NetWorth!!
        return w.getCurrentBalance(principal);
    }

    protected boolean condition(int transactionID) {
        //We'll do the work when incrementRunningTotal gets called.
        return true;
    }
}
