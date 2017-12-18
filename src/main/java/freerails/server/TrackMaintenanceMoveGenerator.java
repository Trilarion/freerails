/*
 * Created on 11-Aug-2003
 *
 */
package freerails.server;

import freerails.move.AddTransactionMove;
import freerails.move.Move;
import freerails.network.MoveReceiver;
import freerails.world.accounts.Bill;
import freerails.world.accounts.Transaction;
import freerails.world.common.Money;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.top.ItemsTransactionAggregator;
import freerails.world.top.SKEY;
import freerails.world.top.World;
import freerails.world.track.TrackConfiguration;
import freerails.world.track.TrackRule;

import static freerails.world.accounts.Transaction.Category.*;

/**
 * This class iterates over the entries in the BankAccount and counts the number
 * of units of each track type, then calculates the cost of maintenance.
 *
 * @author Luke Lindsay
 */
public class TrackMaintenanceMoveGenerator {
    private final MoveReceiver moveReceiver;

    /**
     *
     * @param mr
     */
    public TrackMaintenanceMoveGenerator(MoveReceiver mr) {
        this.moveReceiver = mr;
    }

    /**
     *
     * @param w
     * @param principal
     * @param category
     * @return
     */
    public static AddTransactionMove generateMove(World w,
                                                  FreerailsPrincipal principal, Transaction.Category category) {
        if (TRACK_MAINTENANCE != category && STATION_MAINTENANCE != category) {
            throw new IllegalArgumentException(String.valueOf(category));
        }

        ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(
                w, principal);
        aggregator.setCategory(TRACK);

        long amount = 0;

        for (int i = 0; i < w.size(SKEY.TRACK_RULES); i++) {
            TrackRule trackRule = (TrackRule) w.get(SKEY.TRACK_RULES, i);
            long maintenanceCost = trackRule.getMaintenanceCost().getAmount();

            // Is the track type the category we are interested in?
            boolean rightType = TRACK_MAINTENANCE == category ? !trackRule
                    .isStation() : trackRule.isStation();

            if (rightType) {
                aggregator.setType(i);
                amount += maintenanceCost * aggregator.calculateQuantity()
                        / TrackConfiguration.LENGTH_OF_STRAIGHT_TRACK_PIECE;
            }
        }

        Transaction t = new Bill(new Money(amount), category);

        return new AddTransactionMove(principal, t);
    }

    /**
     *
     * @param w
     */
    public void update(World w) {
        for (int i = 0; i < w.getNumberOfPlayers(); i++) {
            FreerailsPrincipal principal = w.getPlayer(i).getPrincipal();
            Move m = generateMove(w, principal, TRACK_MAINTENANCE);
            moveReceiver.processMove(m);

            m = generateMove(w, principal, STATION_MAINTENANCE);
            moveReceiver.processMove(m);
        }
    }
}