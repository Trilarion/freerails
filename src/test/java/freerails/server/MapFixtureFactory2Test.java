package freerails.server;

import static freerails.server.MapFixtureFactory2.getCopy;
import freerails.world.accounts.AddItemTransaction;
import freerails.world.accounts.Transaction;
import freerails.world.common.Money;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.top.SKEY;
import freerails.world.top.World;
import junit.framework.TestCase;

/**
 * 
 * 
 * @author Luke Lindsay
 * 
 */
public class MapFixtureFactory2Test extends TestCase {
    World w1;

    public void testGetCopy() {
        World w2;
        w1 = getCopy();
        assertNotNull(w1);
        w2 = getCopy();
        assertNotNull(w2);
        assertNotSame(w1, w2);
        assertEquals(w1, w2);

    }

    public void testLists() {

        assertTrue(w1.size(SKEY.CARGO_TYPES) > 0);
        assertTrue(w1.size(SKEY.TRACK_RULES) > 0);
        assertTrue(w1.size(SKEY.TERRAIN_TYPES) > 0);

    }

    public void testMap() {

        assertEquals(w1.getMapWidth(), 50);
        assertEquals(w1.getMapWidth(), 50);

    }

    public void testPlayers() {

        assertEquals(4, w1.getNumberOfPlayers());
    }

    public void testThatStockIsIssued() {
        FreerailsPrincipal p = w1.getPlayer(0).getPrincipal();
        int stock = 0;
        Money cash = w1.getCurrentBalance(p);
        assertEquals(new Money(1000000), cash);
        int numberOfTransactions = w1.getNumberOfTransactions(p);
        assertTrue(numberOfTransactions > 0);
        for (int i = 0; i < numberOfTransactions; i++) {
            Transaction t = w1.getTransaction(p, i);
            if (t.getCategory().equals(Transaction.Category.ISSUE_STOCK)) {
                AddItemTransaction ait = (AddItemTransaction) t;
                stock += ait.getQuantity();
            }
        }
        assertEquals(100000, stock);
    }

    @Override
    protected void setUp() throws Exception {
        // TODO Auto-generated method stub
        super.setUp();
        w1 = getCopy();
    }

}
