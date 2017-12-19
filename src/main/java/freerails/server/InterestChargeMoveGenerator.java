package freerails.server;

import freerails.move.AddTransactionMove;
import freerails.move.Move;
import freerails.network.MoveReceiver;
import freerails.world.accounts.Bill;
import freerails.world.accounts.BondTransaction;
import freerails.world.accounts.Transaction;
import freerails.world.common.Money;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.top.World;

/**
 * This class iterates over the entries in the BankAccount and counts the number
 * of outstanding bonds, then calculates the interest due.
 *
 */
public class InterestChargeMoveGenerator {
    private final MoveReceiver moveReceiver;

    /**
     *
     * @param mr
     */
    public InterestChargeMoveGenerator(MoveReceiver mr) {
        this.moveReceiver = mr;
    }

    private static AddTransactionMove generateMove(World w,
                                                   FreerailsPrincipal principal) {
        long interestDue = 0;

        for (int i = 0; i < w.getNumberOfTransactions(principal); i++) {
            Transaction t = w.getTransaction(principal, i);

            if (t instanceof BondTransaction) {
                BondTransaction bt = (BondTransaction) t;
                int interestRate = bt.getType();
                long bondAmount = BondTransaction.BOND_VALUE_ISSUE.getAmount();
                interestDue += (interestRate * bondAmount / 100)
                        * bt.getQuantity();
            }
        }

        Transaction t = new Bill(new Money(interestDue),
                Transaction.Category.INTEREST_CHARGE);

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