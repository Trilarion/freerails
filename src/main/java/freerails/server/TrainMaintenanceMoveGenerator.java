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
import freerails.world.top.KEY;
import freerails.world.top.NonNullElements;
import freerails.world.top.World;

/**
 * This class iterates over the entries in the BankAccount and counts the number
 * of trains, then calculates the cost of maintenance.
 *
 */
public class TrainMaintenanceMoveGenerator {
    private final MoveReceiver moveReceiver;

    /**
     *
     * @param mr
     */
    public TrainMaintenanceMoveGenerator(MoveReceiver mr) {
        this.moveReceiver = mr;
    }

    private static AddTransactionMove generateMove(World w,
                                                   FreerailsPrincipal principal) {
        NonNullElements trains = new NonNullElements(KEY.TRAINS, w, principal);
        int numberOfTrains = trains.size();
        long amount = numberOfTrains * 5000;
        Transaction t = new Bill(new Money(amount),
                Transaction.Category.TRAIN_MAINTENANCE);

        return new AddTransactionMove(principal, t);
    }

    /**
     *
     * @param w
     */
    public void update(World w) {
        for (int i = 0; i < w.getNumberOfPlayers(); i++) {
            FreerailsPrincipal principal = w.getPlayer(i).getPrincipal();
            Move m = generateMove(w, principal);
            moveReceiver.processMove(m);
        }
    }
}