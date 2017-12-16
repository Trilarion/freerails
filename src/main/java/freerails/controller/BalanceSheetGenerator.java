/*
 * Created on Mar 28, 2004
 */
package freerails.controller;

import freerails.controller.StockPriceCalculator.StockPrice;
import freerails.world.accounts.Transaction;
import freerails.world.common.GameCalendar;
import freerails.world.common.GameTime;
import freerails.world.common.Money;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.top.*;
import freerails.world.track.TrackConfiguration;
import freerails.world.track.TrackRule;

import static freerails.world.accounts.Transaction.Category.*;

/**
 * Generates the balance sheet - note, its fields are read using reflection so
 * don't change their names.
 *
 * @author Luke
 */
public class BalanceSheetGenerator {

    GameTime from;

    GameTime to;

    final ReadOnlyWorld w;

    final FreerailsPrincipal principal;

    private GameCalendar cal;

    public String year;

    public Stats total;

    public Stats ytd;

    public BalanceSheetGenerator(ReadOnlyWorld w, FreerailsPrincipal principal) {
        this.w = w;
        this.principal = principal;
        cal = (GameCalendar) w.get(ITEM.CALENDAR);
        // Calculate totals
        GameTime time = w.currentTime();
        final int startyear = cal.getYear(time.getTicks());
        year = String.valueOf(startyear);
        GameTime startOfYear = new GameTime(cal.getTicks(startyear));

        GameTime[] totalTimeInteval = new GameTime[]{GameTime.BIG_BANG,
                GameTime.END_OF_THE_WORLD};

        total = new Stats(w, principal, totalTimeInteval);

        GameTime[] ytdTimeInteval = new GameTime[]{startOfYear,
                GameTime.END_OF_THE_WORLD};
        ytd = new Stats(w, principal, ytdTimeInteval);

    }

    public static Money calTrackTotal(Transaction.Category category,
                                      ReadOnlyWorld w, FreerailsPrincipal principal, GameTime startTime) {
        ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(
                w, principal);

        aggregator.setCategory(TRACK);
        long amount = 0;

        for (int i = 0; i < w.size(SKEY.TRACK_RULES); i++) {
            TrackRule trackRule = (TrackRule) w.get(SKEY.TRACK_RULES, i);
            long trackValue = trackRule.getPrice().getAmount();

            GameTime[] times = new GameTime[]{startTime,
                    GameTime.END_OF_THE_WORLD};

            aggregator.setType(i);
            aggregator.setTimes(times);
            ItemsTransactionAggregator.QuantitiesAndValues qnv = aggregator
                    .calculateQuantitiesAndValues();
            int quantity = qnv.quantities[0];
            amount += trackValue * quantity
                    / TrackConfiguration.LENGTH_OF_STRAIGHT_TRACK_PIECE;

        }

        return new Money(amount);
    }

    public static class Stats {

        public Stats(ReadOnlyWorld w, FreerailsPrincipal principal,
                     final GameTime[] totalTimeInteval) {
            TransactionAggregator operatingFundsAggregator = new TransactionAggregator(
                    w, principal) {
                @Override
                protected boolean condition(int i) {
                    int transactionTicks = w.getTransactionTimeStamp(principal,
                            i).getTicks();

                    int from = totalTimeInteval[0].getTicks();
                    int to = totalTimeInteval[1].getTicks();
                    return transactionTicks >= from && transactionTicks <= to;
                }
            };

            operatingFunds = operatingFundsAggregator.calculateValue();

            track = calTrackTotal(TRACK, w, principal, totalTimeInteval[0]);

            ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(
                    w, principal);
            aggregator.setTimes(totalTimeInteval);

            aggregator.setCategory(STATIONS);
            stations = aggregator.calculateValue();

            aggregator.setCategory(TRAIN);
            rollingStock = aggregator.calculateValue();

            aggregator.setCategory(INDUSTRIES);
            industries = aggregator.calculateValue();
            aggregator.setCategory(BOND);
            loans = aggregator.calculateValue();
            aggregator.setCategory(ISSUE_STOCK);
            equity = aggregator.calculateValue();

            // If we don't initialize this variable
            // we get a NPE when we don't own any stock in others RRs
            otherRrStock = new Money(0);

            int thisPlayerId = w.getID(principal);

            StockPrice[] stockPrices = (new StockPriceCalculator(w))
                    .calculate();
            for (int playerId = 0; playerId < w.getNumberOfPlayers(); playerId++) {

                aggregator.setCategory(TRANSFER_STOCK);
                aggregator.setType(thisPlayerId);
                int quantity = aggregator.calculateQuantity();
                if (playerId == thisPlayerId) {
                    treasuryStock = new Money(quantity
                            * stockPrices[playerId].currentPrice.getAmount());
                } else {
                    otherRrStock = new Money(quantity
                            * stockPrices[playerId].currentPrice.getAmount()
                            + otherRrStock.getAmount());
                }
            }
            calProfit();

        }

        public Money operatingFunds;

        public Money track;

        public Money stations;

        public Money rollingStock;

        public Money industries;

        public Money loans;

        public Money equity;

        public Money treasuryStock;

        public Money otherRrStock;

        public Money profit;

        private void calProfit() {
            long profitValue = operatingFunds.getAmount() + track.getAmount()
                    + stations.getAmount() + rollingStock.getAmount()
                    + industries.getAmount() + loans.getAmount()
                    + equity.getAmount() + treasuryStock.getAmount()
                    + otherRrStock.getAmount();
            profit = new Money(profitValue);
        }

    }

}
