/*
 * Created on 11-Aug-2003
 *
 */
package jfreerails.server;

import jfreerails.move.AddTransactionMove;
import jfreerails.move.Move;
import jfreerails.network.MoveReceiver;
import jfreerails.world.accounts.Bill;
import jfreerails.world.accounts.Transaction;
import jfreerails.world.common.Money;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.KEY;
import jfreerails.world.top.NonNullElements;
import jfreerails.world.top.World;


/** This class iterates over the entries in the BankAccount
 * and counts the number of trains, then
 * calculates the cost of maintenance.
 *
 * @author Luke Lindsay
 *
 */
public class TrainMaintenanceMoveGenerator {
    private final MoveReceiver moveReceiver;

    public TrainMaintenanceMoveGenerator(MoveReceiver mr) {
        this.moveReceiver = mr;
    }

    private static AddTransactionMove generateMove(World w,
        FreerailsPrincipal principal) {
        NonNullElements trains = new NonNullElements(KEY.TRAINS, w, principal);
        int numberOfTrains = trains.size();
        long amount = numberOfTrains * 5000;
        Transaction t = new Bill(new Money(amount),
                Transaction.TRAIN_MAINTENANCE);

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