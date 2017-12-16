/*
 * Created on 07-Jul-2003
 *
 */
package freerails.move;

import freerails.world.accounts.Bill;
import freerails.world.accounts.Receipt;
import freerails.world.accounts.Transaction;
import freerails.world.common.Money;
import freerails.world.player.Player;
import freerails.world.top.MapFixtureFactory;

/**
 * JUnit test.
 *
 * @author Luke Lindsay
 */
public class AddTransactionMoveTest extends AbstractMoveTestCase {
    @Override
    public void testMove() {
        Money currentBalance = getWorld().getCurrentBalance(
                MapFixtureFactory.TEST_PRINCIPAL);
        assertEquals(new Money(0), currentBalance);

        Transaction t = new Receipt(new Money(100),
                Transaction.Category.MISC_INCOME);
        Move m = new AddTransactionMove(MapFixtureFactory.TEST_PRINCIPAL, t);
        assertTryMoveIsOk(m);
        assertTryUndoMoveFails(m);
        assertDoMoveIsOk(m);
        currentBalance = getWorld().getCurrentBalance(
                MapFixtureFactory.TEST_PRINCIPAL);
        assertEquals(new Money(100), currentBalance);

        final Player PLAYER_WITHOUT_ACCOUNT = new Player(
                "PLAYER_WITHOUT_ACCOUNT", 4);

        assertSurvivesSerialisation(m);

        Move m2 = new AddTransactionMove(PLAYER_WITHOUT_ACCOUNT.getPrincipal(),
                t);
        assertTryMoveFails(m2);

        assertOkAndRepeatable(m);
    }

    public void testConstrainedMove() {
        Money currentBalance = getWorld().getCurrentBalance(
                MapFixtureFactory.TEST_PRINCIPAL);
        assertEquals(new Money(0), currentBalance);

        Transaction t = new Bill(new Money(100),
                Transaction.Category.MISC_INCOME);
        Move m = new AddTransactionMove(MapFixtureFactory.TEST_PRINCIPAL, t,
                true);

        // This move should fail since there is no money in the account and
        // it is constrained is set to true.
        assertTryMoveFails(m);
    }
}