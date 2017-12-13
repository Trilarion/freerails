/*
 * Created on 07-Jul-2003
 *
 */
package jfreerails.move;

import jfreerails.world.accounts.BankAccount;
import jfreerails.world.accounts.InitialDeposit;
import jfreerails.world.accounts.Transaction;
import jfreerails.world.accounts.AddItemTransaction;
import jfreerails.world.common.GameTime;
import jfreerails.world.player.Player;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ITEM;

/**
 * @author Luke Lindsay
 *
 */
public class AddTransactionMoveTest extends AbstractMoveTestCase {
    public void testMove() {
        BankAccount account = (BankAccount)getWorld().get(KEY.BANK_ACCOUNTS, 0,
		testPlayer.getPrincipal());
        assertTrue(0 == account.getCurrentBalance());

        Transaction t = new InitialDeposit((GameTime)
		getWorld().get(ITEM.TIME, Player.AUTHORITATIVE), 100);
        Move m = new AddTransactionMove(0, t, testPlayer.getPrincipal());
        assertTryMoveIsOk(m);
        assertTryUndoMoveFails(m);
        assertDoMoveIsOk(m);
        assertTrue(100 == account.getCurrentBalance());

        Move m2 = new AddTransactionMove(5, t, testPlayer.getPrincipal());
        assertTryMoveFails(m2);
        assertEqualsSurvivesSerialisation(m);

        assertOkAndRepeatable(m);
    }

    public void testConstrainedMove() {
        BankAccount account = (BankAccount)getWorld().get(KEY.BANK_ACCOUNTS, 0,
		testPlayer.getPrincipal());
        assertTrue(0 == account.getCurrentBalance());

	GameTime now = (GameTime) getWorld().get(ITEM.TIME,
		testPlayer.getPrincipal());
	Transaction t = new AddItemTransaction(now, AddItemTransaction.TRACK,
		0, 1, -100);
        Move m = new AddTransactionMove(0, t, true, testPlayer.getPrincipal());

        //This move should fail since there is no money in the account and 
        //it is constrained is set to true.
        assertTryMoveFails(m);
    }
}
