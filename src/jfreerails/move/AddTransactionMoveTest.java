/*
 * Created on 07-Jul-2003
 *
 */
package jfreerails.move;

import jfreerails.world.accounts.Bill;
import jfreerails.world.accounts.Receipt;
import jfreerails.world.accounts.Transaction;
import jfreerails.world.common.Money;
import jfreerails.world.player.Player;
import jfreerails.world.top.MapFixtureFactory;


/**
 *  JUnit test.
 * @author Luke Lindsay
 *
 */
public class AddTransactionMoveTest extends AbstractMoveTestCase {
    public void testMove() {
        Money currentBalance = getWorld().getCurrentBalance(MapFixtureFactory.TEST_PRINCIPAL);
        assertEquals(new Money(0), currentBalance);

        Transaction t = new Receipt(new Money(100), Transaction.MISC_INCOME);
        Move m = new AddTransactionMove(MapFixtureFactory.TEST_PRINCIPAL, t);
        assertTryMoveIsOk(m);
        assertTryUndoMoveFails(m);
        assertDoMoveIsOk(m);
        currentBalance = getWorld().getCurrentBalance(MapFixtureFactory.TEST_PRINCIPAL);
        assertEquals(new Money(100), currentBalance);

        final Player PLAYER_WITHOUT_ACCOUNT = new Player("PLAYER_WITHOUT_ACCOUNT",
                (new Player("PLAYER_WITHOUT_ACCOUNT")).getPublicKey(), 4);

        assertEqualsSurvivesSerialisation(m);

        Move m2 = new AddTransactionMove(PLAYER_WITHOUT_ACCOUNT.getPrincipal(),
                t);
        assertTryMoveFails(m2);

        assertOkAndRepeatable(m);
    }

    public void testConstrainedMove() {
        Money currentBalance = getWorld().getCurrentBalance(MapFixtureFactory.TEST_PRINCIPAL);
        assertEquals(new Money(0), currentBalance);

        Transaction t = new Bill(new Money(100), Transaction.MISC_INCOME);
        Move m = new AddTransactionMove(MapFixtureFactory.TEST_PRINCIPAL, t,
                true);

        //This move should fail since there is no money in the account and 
        //it is constrained is set to true.
        assertTryMoveFails(m);
    }
}