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

    private final Money operatingFunds;
    private final Money track;
    private final Money stations;
    private final Money rollingStock;
    private final Money industries;
    public final Money loans;
    public final Money equity;
    private Money treasuryStock;
    private Money otherRrStock;

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

        // If we don't initialize this variable we get a NPE when we don't own any stock in others RRs
        otherRrStock = Money.ZERO;

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
    }

    private static class MyTransactionAggregator extends TransactionAggregator {
        private final GameTime[] totalTimeInterval;

        private MyTransactionAggregator(ReadOnlyWorld world, FreerailsPrincipal principal, GameTime[] totalTimeInterval) {
            super(world, principal);
            this.totalTimeInterval = totalTimeInterval;
        }

        @Override
        protected boolean condition(int transactionID) {
            int transactionTicks = world.getTransactionTimeStamp(principal, transactionID).getTicks();

            int from = totalTimeInterval[0].getTicks();
            int to = totalTimeInterval[1].getTicks();
            return transactionTicks >= from && transactionTicks <= to;
        }
    }
}
