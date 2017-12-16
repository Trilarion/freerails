/*
 * Created on 20-Mar-2003
 *
 */
package freerails.world.top;

import freerails.util.Utils;
import freerails.world.accounts.AddItemTransaction;
import freerails.world.accounts.Receipt;
import freerails.world.accounts.Transaction;
import freerails.world.accounts.Transaction.Category;
import freerails.world.common.Activity;
import freerails.world.common.FreerailsSerializable;
import freerails.world.common.Money;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.player.Player;
import junit.framework.TestCase;

/**
 * Junit test.
 * 
 * @author Luke
 * 
 */
public class WorldImplTest extends TestCase {
    private final FreerailsSerializable fs = new TestState(1);

    public void testGet() {
        WorldImpl w = new WorldImpl();
        w.add(SKEY.TERRAIN_TYPES, fs);
        assertEquals(w.get(SKEY.TERRAIN_TYPES, 0), fs);
    }

    public void testConstructor() {
        World w = new WorldImpl();
        assertEquals("The width should be zero", 0, w.getMapWidth());
        assertEquals("The height should be zero", 0, w.getMapHeight());
    }

    /**
     * Tests that changing the object returned by defensiveCopy() does not alter
     * the world object that was copied.
     */
    public void testDefensiveCopy() {
        World original;
        World copy;
        original = new WorldImpl();
        copy = original.defensiveCopy();
        assertNotSame("The copies should be different objects.", original, copy);
        assertEquals("The copies should be logically equal.", original, copy);

        copy.add(SKEY.TERRAIN_TYPES, fs);

        assertFalse(original.equals(copy));
        assertFalse(copy.equals(original));
        assertEquals(1, copy.size(SKEY.TERRAIN_TYPES));
        assertEquals(0, original.size(SKEY.TERRAIN_TYPES));
    }

    public void testEquals() {
        World original;
        World copy;
        original = new WorldImpl();
        copy = original.defensiveCopy();

        Player player = new Player("Name", 0);
        int index = copy.addPlayer(player);
        assertEquals(index, 0);
        assertFalse(copy.equals(original));
        original.addPlayer(player);
        assertEquals("The copies should be logically equal.", original, copy);

        assertTrue(Utils.equalsBySerialization(original, copy));

        Transaction t = new Receipt(new Money(100),
                Transaction.Category.MISC_INCOME);
        copy.addTransaction(player.getPrincipal(), t);
        assertEquals(new Money(100), copy.getCurrentBalance(player
                .getPrincipal()));
        assertFalse(copy.equals(original));

    }

    public void testEquals2() {
        World original;
        World copy, copy2;
        original = new WorldImpl();
        copy = original.defensiveCopy();
        copy2 = original.defensiveCopy();
        // Test adding players.
        Player a = new Player("Fred");
        Player b = new Player("John");
        original.addPlayer(a);
        copy.addPlayer(b);
        assertFalse(copy.equals(original));
        copy.removeLastPlayer();
        assertTrue(copy2.equals(copy));
        copy.addPlayer(a);
        assertTrue(copy.equals(original));

    }

    public void testActivityLists() {
        World w = new WorldImpl();
        Player player = new Player("Name", 0);
        w.addPlayer(player);
        FreerailsPrincipal principal = player.getPrincipal();

        // Test adding activities.
        assertEquals(0, w.size(principal));
        Activity act = new TestActivity(30);
        int actIndex = w.addActiveEntity(principal, act);
        assertEquals(0, actIndex);
        assertEquals(1, w.size(principal));
        actIndex = w.addActiveEntity(principal, act);
        assertEquals(1, actIndex);
        assertEquals(2, w.size(principal));

        // Then removing them.
        Activity expected = new TestActivity(30);
        assertEquals(expected, act);
        Activity actual = w.removeLastActiveEntity(principal);
        assertEquals(actual, expected);
        assertEquals(1, w.size(principal));

    }

    public static class TestState implements FreerailsSerializable {

        private static final long serialVersionUID = 5122023949873919060L;

        public final int x;

        public TestState(int x) {
            this.x = x;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof TestState))
                return false;

            final TestState testState = (TestState) o;

            if (x != testState.x)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            return x;
        }

    }

    public static class TestActivity implements Activity {

        private static final long serialVersionUID = 1298936498785131183L;

        private final double duration;

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof TestActivity))
                return false;

            final TestActivity testActivity = (TestActivity) o;

            if (duration != testActivity.duration)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            return (int) duration;
        }

        public TestActivity(int duration) {
            this.duration = duration;
        }

        public double duration() {
            return duration;
        }

        public FreerailsSerializable getState(double dt) {
            return new TestState((int) dt);
        }

        @Override
        public String toString() {
            return getClass().getName() + "{" + duration + "}";
        }

    }

    public void testBoundsContain() {
        World w = new WorldImpl();
        assertFalse(w.boundsContain(1, 1));
        assertFalse(w.boundsContain(0, 0));
        assertFalse(w.boundsContain(-1, -1));
        w = new WorldImpl(5, 10);
        assertTrue(w.boundsContain(0, 0));
        assertTrue(w.boundsContain(4, 9));
        assertFalse(w.boundsContain(-1, -1));
        assertFalse(w.boundsContain(5, 10));
    }

    public void testBankAccount() {
        WorldImpl world = new WorldImpl();
        Player p = new Player("Test", 0);
        int playerID = world.addPlayer(p);
        assertEquals(0, playerID);
        FreerailsPrincipal fp = world.getPlayer(playerID).getPrincipal();
        Transaction t = new AddItemTransaction(Category.BOND, 1, 2, new Money(
                100));
        assertEquals(new Money(0), world.getCurrentBalance(fp));
        world.addTransaction(fp, t);
        assertEquals(1, world.getNumberOfTransactions(fp));
        assertEquals(new Money(100), world.getCurrentBalance(fp));
        Transaction t2 = world.getTransaction(fp, 0);
        assertEquals(t, t2);
        Transaction t3 = world.removeLastTransaction(fp);
        assertEquals(t, t3);
        assertEquals(new Money(0), world.getCurrentBalance(fp));

    }
}
