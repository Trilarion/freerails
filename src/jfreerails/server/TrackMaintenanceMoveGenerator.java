/*
 * Created on 11-Aug-2003
 *
 */
package jfreerails.server;

import jfreerails.controller.MoveReceiver;
import jfreerails.move.AddTransactionMove;
import jfreerails.move.ChangeTrackPieceCompositeMove;
import jfreerails.move.Move;
import jfreerails.world.accounts.Bill;
import jfreerails.world.accounts.Transaction;
import jfreerails.world.common.Money;
import jfreerails.world.player.FreerailsPrincipal;
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

    public static AddTransactionMove generateMove(World w,
        FreerailsPrincipal principal) {
        int[] track = ChangeTrackPieceCompositeMove.calulateNumberOfEachTrackType(w,
                principal);
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

        return new AddTransactionMove(principal, t);
    }

    public void update(World w) {
        for (int i = 0; i < w.getNumberOfPlayers(); i++) {
            FreerailsPrincipal principal = w.getPlayer(i).getPrincipal();
            Move m = generateMove(w, principal);
            moveReceiver.processMove(m);
        }
    }
}