/*
 * Created on 11-Aug-2003
 *
 */
package jfreerails.server;

import jfreerails.controller.MoveReceiver;
import jfreerails.move.AddTransactionMove;
import jfreerails.move.Move;
import jfreerails.world.accounts.Bill;
import jfreerails.world.accounts.Transaction;
import jfreerails.world.common.Money;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.ItemsTransactionAggregator;
import jfreerails.world.top.SKEY;
import jfreerails.world.top.World;
import jfreerails.world.track.TrackConfiguration;
import jfreerails.world.track.TrackRule;


/** This class iterates over the entries in the BankAccount
 * and counts the number of units of each track type, then
 * calculates the cost of maintenance.
 *
 * @author Luke Lindsay
 *
 */
public class TrackMaintenanceMoveGenerator {
    private final MoveReceiver moveReceiver;

    public TrackMaintenanceMoveGenerator(MoveReceiver mr) {
        this.moveReceiver = mr;
    }

    public static AddTransactionMove generateMove(World w,
        FreerailsPrincipal principal, int category) {
        if (Transaction.TRACK_MAINTENANCE != category &&
                Transaction.STATION_MAINTENANCE != category) {
            throw new IllegalArgumentException(String.valueOf(category));
        }

        //int[] track = ItemsTransactionAggregator.calulateNumberOfEachTrackType(w,
        //        principal, 0);
        ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(w,
                principal);
        aggregator.setStartYear(0);
        aggregator.setCategory(Transaction.TRACK);

        long amount = 0;

        for (int i = 0; i < w.size(SKEY.TRACK_RULES); i++) {
            TrackRule trackRule = (TrackRule)w.get(SKEY.TRACK_RULES, i);
            long maintenanceCost = trackRule.getMaintenanceCost().getAmount();

            //Is the track type the category we are interested in?
            boolean rightType = Transaction.TRACK_MAINTENANCE == category
                ? !trackRule.isStation() : trackRule.isStation();

            if (rightType) {
                aggregator.setType(i);
                amount += maintenanceCost * aggregator.calulateQuantity() / TrackConfiguration.LENGTH_OF_STRAIGHT_TRACK_PIECE;
            }
        }

        Transaction t = new Bill(new Money(amount), category);

        return new AddTransactionMove(principal, t);
    }

    public void update(World w) {
        for (int i = 0; i < w.getNumberOfPlayers(); i++) {
            FreerailsPrincipal principal = w.getPlayer(i).getPrincipal();
            Move m = generateMove(w, principal, Transaction.TRACK_MAINTENANCE);
            moveReceiver.processMove(m);

            m = generateMove(w, principal, Transaction.STATION_MAINTENANCE);
            moveReceiver.processMove(m);
        }
    }
}