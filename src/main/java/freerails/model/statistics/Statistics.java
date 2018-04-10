package freerails.model.statistics;

import freerails.model.WorldConstants;
import freerails.model.finances.*;
import freerails.model.track.TrackRule;
import freerails.model.world.ReadOnlyWorld;
import freerails.model.game.GameTime;
import freerails.model.player.FreerailsPrincipal;
import freerails.model.world.SharedKey;

/**
 *
 */
public class Statistics {

    public final Money loans;
    public final Money equity;

    /**
     * @param world
     * @param principal
     * @param totalTimeInterval
     */
    public Statistics(ReadOnlyWorld world, FreerailsPrincipal principal, final GameTime[] totalTimeInterval) {
        TransactionAggregator operatingFundsAggregator = new MyTransactionAggregator(world, principal, totalTimeInterval);

        Money operatingFunds = operatingFundsAggregator.calculateValue();

        Money track = calculateTrackTotal(world, principal, totalTimeInterval[0]);

        ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(world, principal);
        aggregator.setTimes(totalTimeInterval);

        aggregator.setCategory(TransactionCategory.STATIONS);
        Money stations = aggregator.calculateValue();

        aggregator.setCategory(TransactionCategory.TRAIN);
        Money rollingStock = aggregator.calculateValue();

        aggregator.setCategory(TransactionCategory.INDUSTRIES);
        Money industries = aggregator.calculateValue();
        aggregator.setCategory(TransactionCategory.BOND);
        loans = aggregator.calculateValue();
        aggregator.setCategory(TransactionCategory.ISSUE_STOCK);
        equity = aggregator.calculateValue();

        // If we don't initialize this variable we get a NPE when we don't own any stock in others RRs
        Money otherRrStock = Money.ZERO;

        int thisPlayerId = world.getID(principal);

        StockPrice[] stockPrices = (new StockPriceCalculator(world)).calculate();
        for (int playerId = 0; playerId < world.getNumberOfPlayers(); playerId++) {

            aggregator.setCategory(TransactionCategory.TRANSFER_STOCK);
            aggregator.setType(thisPlayerId);
            int quantity = aggregator.calculateQuantity();
            if (playerId == thisPlayerId) {
                Money treasuryStock = Money.multiply(stockPrices[playerId].currentPrice, quantity);
            } else {
                otherRrStock = Money.add(Money.multiply(stockPrices[playerId].currentPrice, quantity), otherRrStock);
            }
        }
    }

    /**
     * @param world
     * @param principal
     * @param startTime
     * @return
     */
    public static Money calculateTrackTotal(ReadOnlyWorld world, FreerailsPrincipal principal, GameTime startTime) {

        ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(world, principal);
        aggregator.setCategory(TransactionCategory.TRACK);
        long amount = 0;

        for (int i = 0; i < world.size(SharedKey.TrackRules); i++) {
            TrackRule trackRule = (TrackRule) world.get(SharedKey.TrackRules, i);
            // TODO Money arithmetics
            long trackValue = trackRule.getPrice().amount;

            GameTime[] times = new GameTime[]{startTime, GameTime.DOOMSDAY};

            aggregator.setType(i);
            aggregator.setTimes(times);
            int quantity = aggregator.calculateQuantity();
            amount += trackValue * quantity / WorldConstants.LENGTH_OF_STRAIGHT_TRACK_PIECE;
        }

        return new Money(amount);
    }
}
