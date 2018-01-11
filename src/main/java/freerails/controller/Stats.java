package freerails.controller;

import freerails.world.ItemsTransactionAggregator;
import freerails.world.ReadOnlyWorld;
import freerails.world.TransactionAggregator;
import freerails.world.finances.Money;
import freerails.world.finances.TransactionCategory;
import freerails.world.game.GameTime;
import freerails.world.player.FreerailsPrincipal;

/**
 *
 */
public class Stats {

    public final Money operatingFunds;
    public final Money track;
    public final Money stations;
    public final Money rollingStock;
    public final Money industries;
    public final Money loans;
    public final Money equity;
    public Money treasuryStock;
    public Money otherRrStock;
    public Money profit;

    /**
     * @param world
     * @param principal
     * @param totalTimeInterval
     */
    public Stats(ReadOnlyWorld world, FreerailsPrincipal principal, final GameTime[] totalTimeInterval) {
        TransactionAggregator operatingFundsAggregator = new MyTransactionAggregator(world, principal, totalTimeInterval);

        operatingFunds = operatingFundsAggregator.calculateValue();

        track = BalanceSheetGenerator.calTrackTotal(world, principal, totalTimeInterval[0]);

        ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(world, principal);
        aggregator.setTimes(totalTimeInterval);

        aggregator.setCategory(TransactionCategory.STATIONS);
        stations = aggregator.calculateValue();

        aggregator.setCategory(TransactionCategory.TRAIN);
        rollingStock = aggregator.calculateValue();

        aggregator.setCategory(TransactionCategory.INDUSTRIES);
        industries = aggregator.calculateValue();
        aggregator.setCategory(TransactionCategory.BOND);
        loans = aggregator.calculateValue();
        aggregator.setCategory(TransactionCategory.ISSUE_STOCK);
        equity = aggregator.calculateValue();

        // If we don't initialize this variable
        // we get a NPE when we don't own any stock in others RRs
        otherRrStock = new Money(0);

        int thisPlayerId = world.getID(principal);

        StockPriceCalculator.StockPrice[] stockPrices = (new StockPriceCalculator(world)).calculate();
        for (int playerId = 0; playerId < world.getNumberOfPlayers(); playerId++) {

            aggregator.setCategory(TransactionCategory.TRANSFER_STOCK);
            aggregator.setType(thisPlayerId);
            int quantity = aggregator.calculateQuantity();
            if (playerId == thisPlayerId) {
                treasuryStock = new Money(quantity * stockPrices[playerId].currentPrice.getAmount());
            } else {
                otherRrStock = new Money(quantity * stockPrices[playerId].currentPrice.getAmount() + otherRrStock.getAmount());
            }
        }
        calProfit();

    }

    private void calProfit() {
        long profitValue = operatingFunds.getAmount() + track.getAmount() + stations.getAmount() + rollingStock.getAmount() + industries.getAmount() + loans.getAmount() + equity.getAmount() + treasuryStock.getAmount() + otherRrStock.getAmount();
        profit = new Money(profitValue);
    }

    private static class MyTransactionAggregator extends TransactionAggregator {
        private final GameTime[] totalTimeInterval;

        public MyTransactionAggregator(ReadOnlyWorld world, FreerailsPrincipal principal, GameTime[] totalTimeInterval) {
            super(world, principal);
            this.totalTimeInterval = totalTimeInterval;
        }

        @Override
        protected boolean condition(int transactionID) {
            int transactionTicks = w.getTransactionTimeStamp(principal, transactionID).getTicks();

            int from = totalTimeInterval[0].getTicks();
            int to = totalTimeInterval[1].getTicks();
            return transactionTicks >= from && transactionTicks <= to;
        }
    }
}
