/*
 * Created on 11-Aug-2003
 *
 */
package jfreerails.server;

import jfreerails.controller.MoveReceiver;
import jfreerails.move.AddTransactionMove;
import jfreerails.move.Move;
import jfreerails.world.accounts.AddItemTransaction;
import jfreerails.world.accounts.Bill;
import jfreerails.world.accounts.Transaction;
import jfreerails.world.common.Money;
import jfreerails.world.player.Player;
import jfreerails.world.top.SKEY;
import jfreerails.world.top.World;
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

    public static AddTransactionMove generateMove(World w) {
        int[] track = calulateNumberOfEachTrackType(w);
        long amount = 0;

        for (int i = 0; i < track.length; i++) {
            TrackRule trackRule = (TrackRule)w.get(SKEY.TRACK_RULES, i);
            long maintenanceCost = trackRule.getMaintenanceCost().getAmount();

            if (track[i] > 0) {
                //                            System.out.println(track[i] + " " + trackRule.getTypeName() +
                //                                " maintenance cost of " + maintenanceCost * track[i]);
                amount += maintenanceCost * track[i];
            }
        }

        Transaction t = new Bill(new Money(amount));

        return new AddTransactionMove(Player.TEST_PRINCIPAL, t);
    }

    public static int[] calulateNumberOfEachTrackType(World w) {
        int[] unitsOfTrack = new int[w.size(SKEY.TRACK_RULES)];

        for (int i = 0; i < w.getNumberOfTransactions(Player.TEST_PRINCIPAL);
                i++) {
            Transaction t = w.getTransaction(i, Player.TEST_PRINCIPAL);

            if (t instanceof AddItemTransaction) {
                AddItemTransaction addItemTransaction = (AddItemTransaction)t;

                if (AddItemTransaction.TRACK == addItemTransaction.getCategory()) {
                    unitsOfTrack[addItemTransaction.getType()] += addItemTransaction.getQuantity();
                }
            }
        }

        return unitsOfTrack;
    }

    public void update(World w) {
        Move m = generateMove(w);
        moveReceiver.processMove(m);
    }
}