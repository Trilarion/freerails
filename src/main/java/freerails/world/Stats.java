package freerails.world;

import freerails.world.finances.*;
import freerails.world.track.TrackRule;
import freerails.world.world.ReadOnlyWorld;
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

        track = calTrackTotal(world, principal, totalTimeInterval[0]);

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

        StockPrice[] stockPrices = (new StockPriceCalculator(world)).calculate();
        for (int playerId = 0; playerId < world.getNumberOfPlayers(); playerId++) {

            aggregator.setCategory(TransactionCategory.TRANSFER_STOCK);
            aggregator.setType(thisPlayerId);
            int quantity = aggregator.calculateQuantity();
            if (playerId == thisPlayerId) {
                treasuryStock = Money.multiply(stockPrices[playerId].currentPrice, quantity);
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
    public static Money calTrackTotal(ReadOnlyWorld world, FreerailsPrincipal principal, GameTime startTime) {

        ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(world, principal);
        aggregator.setCategory(TransactionCategory.TRACK);
        long amount = 0;

        for (int i = 0; i < world.size(SKEY.TRACK_RULES); i++) {
            TrackRule trackRule = (TrackRule) world.get(SKEY.TRACK_RULES, i);
            // TODO Money arithmetics
            long trackValue = trackRule.getPrice().amount;

            GameTime[] times = new GameTime[]{startTime, GameTime.DOOMSDAY};

            aggregator.setType(i);
            aggregator.setTimes(times);
            QuantitiesAndValues qnv = aggregator.calculateQuantitiesAndValues();
            int quantity = qnv.quantities[0];
            amount += trackValue * quantity / WorldConstants.LENGTH_OF_STRAIGHT_TRACK_PIECE;
        }

        return new Money(amount);
    }
}
