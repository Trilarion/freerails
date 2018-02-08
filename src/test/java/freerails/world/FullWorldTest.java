/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 *
 */
package freerails.world;

import freerails.util.Point2D;
import freerails.util.Utils;
import freerails.world.finances.*;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.player.Player;
import junit.framework.TestCase;

import java.io.Serializable;

/**
 * Test for FullWorld.
 */
public class FullWorldTest extends TestCase {

    private final Serializable fs = new TestState(1);

    /**
     *
     */
    public void testGet() {
        FullWorld fullWorld = new FullWorld();
        fullWorld.add(SKEY.TERRAIN_TYPES, fs);
        assertEquals(fullWorld.get(SKEY.TERRAIN_TYPES, 0), fs);
    }

    /**
     *
     */
    public void testConstructor() {
        World world = new FullWorld();
        assertEquals("The width should be zero", 0, world.getMapWidth());
        assertEquals("The height should be zero", 0, world.getMapHeight());
    }

    /**
     * Tests that changing the object returned by defensiveCopy() does not alter
     * the world object that was copied.
     */
    public void testDefensiveCopy() {
        World original;
        World copy;
        original = new FullWorld();
        copy = original.defensiveCopy();
        assertNotSame("The copies should be different objects.", original, copy);
        assertEquals("The copies should be logically equal.", original, copy);

        copy.add(SKEY.TERRAIN_TYPES, fs);

        assertFalse(original.equals(copy));
        assertFalse(copy.equals(original));
        assertEquals(1, copy.size(SKEY.TERRAIN_TYPES));
        assertEquals(0, original.size(SKEY.TERRAIN_TYPES));
    }

    /**
     *
     */
    public void testEquals() {
        World original;
        World copy;
        original = new FullWorld();
        copy = original.defensiveCopy();

        Player player = new Player("Name", 0);
        int index = copy.addPlayer(player);
        assertEquals(index, 0);
        assertFalse(copy.equals(original));
        original.addPlayer(player);
        assertEquals("The copies should be logically equal.", original, copy);

        assertTrue(Utils.equalsBySerialization(original, copy));

        Transaction transaction = new MoneyTransaction(new Money(100),
                TransactionCategory.MISC_INCOME);
        copy.addTransaction(player.getPrincipal(), transaction);
        assertEquals(new Money(100), copy.getCurrentBalance(player
                .getPrincipal()));
        assertFalse(copy.equals(original));

    }

    /**
     *
     */
    public void testEquals2() {
        World original;
        World copy, copy2;
        original = new FullWorld();
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

    /**
     *
     */
    public void testActivityLists() {
        World world = new FullWorld();
        Player player = new Player("Name", 0);
        world.addPlayer(player);
        FreerailsPrincipal principal = player.getPrincipal();

        // Test adding activities.
        assertEquals(0, world.size(principal));
        Activity act = new TestActivity(30);
        int actIndex = world.addActiveEntity(principal, act);
        assertEquals(0, actIndex);
        assertEquals(1, world.size(principal));
        actIndex = world.addActiveEntity(principal, act);
        assertEquals(1, actIndex);
        assertEquals(2, world.size(principal));

        // Then removing them.
        Activity expected = new TestActivity(30);
        assertEquals(expected, act);
        Activity actual = world.removeLastActiveEntity(principal);
        assertEquals(actual, expected);
        assertEquals(1, world.size(principal));

    }

    /**
     *
     */
    public void testBoundsContain() {
        World world = new FullWorld();
        assertFalse(world.boundsContain(new Point2D(1, 1)));
        assertFalse(world.boundsContain(Point2D.ZERO));
        assertFalse(world.boundsContain(new Point2D(-1, -1)));
        world = new FullWorld(5, 10);
        assertTrue(world.boundsContain(Point2D.ZERO));
        assertTrue(world.boundsContain(new Point2D(4, 9)));
        assertFalse(world.boundsContain(new Point2D(-1, -1)));
        assertFalse(world.boundsContain(new Point2D(5, 10)));
    }

    /**
     *
     */
    public void testBankAccount() {
        FullWorld world = new FullWorld();
        Player player = new Player("Test", 0);
        int playerID = world.addPlayer(player);
        assertEquals(0, playerID);
        FreerailsPrincipal fp = world.getPlayer(playerID).getPrincipal();
        Transaction transaction = new ItemTransaction(TransactionCategory.BOND, 1, 2, new Money(
                100));
        assertEquals(new Money(0), world.getCurrentBalance(fp));
        world.addTransaction(fp, transaction);
        assertEquals(1, world.getNumberOfTransactions(fp));
        assertEquals(new Money(100), world.getCurrentBalance(fp));
        Transaction t2 = world.getTransaction(fp, 0);
        assertEquals(transaction, t2);
        Transaction t3 = world.removeLastTransaction(fp);
        assertEquals(transaction, t3);
        assertEquals(new Money(0), world.getCurrentBalance(fp));

    }

    /**
     *
     */
    static class TestState implements Serializable {

        private static final long serialVersionUID = 5122023949873919060L;

        /**
         *
         */
        private final int x;

        /**
         * @param x
         */
        TestState(int x) {
            this.x = x;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof TestState))
                return false;

            final TestState testState = (TestState) obj;

            return x == testState.x;
        }

        @Override
        public int hashCode() {
            return x;
        }

    }

    /**
     *
     */
    public static class TestActivity implements Activity {

        private static final long serialVersionUID = 1298936498785131183L;

        private final double duration;

        /**
         * @param duration
         */
        public TestActivity(int duration) {
            this.duration = duration;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof TestActivity))
                return false;

            final TestActivity testActivity = (TestActivity) obj;

            return !(duration != testActivity.duration);
        }

        @Override
        public int hashCode() {
            return (int) duration;
        }

        /**
         * @return
         */
        public double duration() {
            return duration;
        }

        /**
         * @param dt
         * @return
         */
        public Serializable getState(double dt) {
            return new TestState((int) dt);
        }

        @Override
        public String toString() {
            return getClass().getName() + '{' + duration + '}';
        }

    }
}
