package freerails.model.statistics;

import freerails.model.ModelConstants;
import freerails.model.finances.*;
import freerails.model.finances.transactions.TransactionCategory;
import freerails.model.track.TrackType;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.game.GameTime;
import freerails.model.player.Player;

/**
 *
 */
public class Statistics {

    public final Money loans;
    public final Money equity;

    /**
     * @param world
     * @param player
     * @param totalTimeInterval
     */
    public Statistics(UnmodifiableWorld world, Player player, final GameTime[] totalTimeInterval) {
        Money track = calculateTrackTotal(world, player, totalTimeInterval[0]);

        ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(world, player);
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

        int thisPlayerId = player.getId();

        // TODO use maps instead of arrays
        StockPrice[] stockPrices = (new StockPriceCalculator(world)).calculate();
        for (Player otherPlayer: world.getPlayers()) {
            aggregator.setCategory(TransactionCategory.TRANSFER_STOCK);
            aggregator.setType(thisPlayerId);
            int quantity = aggregator.calculateQuantity();
            if (otherPlayer.getId() == thisPlayerId) {
                Money treasuryStock = Money.multiply(stockPrices[otherPlayer.getId()].currentPrice, quantity);
            } else {
                otherRrStock = Money.add(Money.multiply(stockPrices[otherPlayer.getId()].currentPrice, quantity), otherRrStock);
            }
        }
    }

    /**
     * @param world
     * @param player
     * @param startTime
     * @return
     */
    public static Money calculateTrackTotal(UnmodifiableWorld world, Player player, GameTime startTime) {

        ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(world, player);
        aggregator.setCategory(TransactionCategory.TRACK);
        long amount = 0;

        for (TrackType trackType: world.getTrackTypes()) {
            // TODO Money arithmetic
            long trackValue = trackType.getPurchasingPrice().amount;

            GameTime[] times = new GameTime[]{startTime, GameTime.DOOMSDAY};

            aggregator.setType(trackType.getId());
            aggregator.setTimes(times);
            int quantity = aggregator.calculateQuantity();
            amount += trackValue * quantity / ModelConstants.LENGTH_OF_STRAIGHT_TRACK_PIECE;
        }

        return new Money(amount);
    }
}
