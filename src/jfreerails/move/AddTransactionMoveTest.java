/*
 * Created on 07-Jul-2003
 *
 */
package jfreerails.move;

import jfreerails.world.accounts.BankAccount;
import jfreerails.world.accounts.Bill;
import jfreerails.world.accounts.Receipt;
import jfreerails.world.accounts.Transaction;
import jfreerails.world.common.Money;
import jfreerails.world.top.KEY;


/**
 * @author Luke Lindsay
 *
 */
public class AddTransactionMoveTest extends AbstractMoveTestCase {
    public void testMove() {
        BankAccount account = (BankAccount)world.get(KEY.BANK_ACCOUNTS, 0);
        assertEquals(new Money(0), account.getCurrentBalance());

        Transaction t = new Receipt(new Money(100));
        Move m = new AddTransactionMove(0, t);
        assertTryMoveIsOk(m);
        assertTryUndoMoveFails(m);
        assertDoMoveIsOk(m);
        assertEquals(new Money(100), account.getCurrentBalance());

        Move m2 = new AddTransactionMove(5, t);
        assertTryMoveFails(m2);
        assertEqualsSurvivesSerialisation(m);

        assertOkAndRepeatable(m);
    }

    public void testConstrainedMove() {
        BankAccount account = (BankAccount)world.get(KEY.BANK_ACCOUNTS, 0);
        assertEquals(new Money(0), account.getCurrentBalance());

        Transaction t = new Bill(new Money(100));
        Move m = new AddTransactionMove(0, t, true);

        //This move should fail since there is no money in the account and 
        //it is constrained is set to true.
        assertTryMoveFails(m);
    }
}