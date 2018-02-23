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
package freerails.model.world;

import freerails.model.Activity;
import freerails.util.Vector2D;
import freerails.util.Utils;
import freerails.model.finances.*;
import freerails.model.player.FreerailsPrincipal;
import freerails.model.player.Player;
import junit.framework.TestCase;

import java.io.Serializable;

/**
 * Test for FullWorld.
 */
public class FullWorldTest extends TestCase {

    private static final Serializable fs = new TestState(1);

    /**
     *
     */
    public void testGet() {
        FullWorld fullWorld = new FullWorld();
        fullWorld.add(SharedKey.TerrainTypes, fs);
        assertEquals(fullWorld.get(SharedKey.TerrainTypes, 0), fs);
    }

    /**
     *
     */
    public void testConstructor() {
        World world = new FullWorld();
        assertEquals(world.getMapSize(), Vector2D.ZERO);
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

        copy.add(SharedKey.TerrainTypes, fs);

        assertFalse(original.equals(copy));
        assertFalse(copy.equals(original));
        assertEquals(1, copy.size(SharedKey.TerrainTypes));
        assertEquals(0, original.size(SharedKey.TerrainTypes));
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

        Transaction transaction = new MoneyTransaction(new Money(100), TransactionCategory.MISC_INCOME);
        copy.addTransaction(player.getPrincipal(), transaction);
        assertEquals(new Money(100), copy.getCurrentBalance(player.getPrincipal()));
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
        assertFalse(world.boundsContain(new Vector2D(1, 1)));
        assertFalse(world.boundsContain(Vector2D.ZERO));
        assertFalse(world.boundsContain(new Vector2D(-1, -1)));
        world = new FullWorld(new Vector2D(5, 10));
        assertTrue(world.boundsContain(Vector2D.ZERO));
        assertTrue(world.boundsContain(new Vector2D(4, 9)));
        assertFalse(world.boundsContain(new Vector2D(-1, -1)));
        assertFalse(world.boundsContain(new Vector2D(5, 10)));
    }

    /**
     *
     */
    public void testBankAccount() {
        FullWorld world = new FullWorld();
        Player player = new Player("Test", 0);
        int playerID = world.addPlayer(player);
        assertEquals(0, playerID);
        FreerailsPrincipal principal = world.getPlayer(playerID).getPrincipal();
        Transaction transaction = new ItemTransaction(TransactionCategory.BOND, 1, 2, new Money(100));
        assertEquals(new Money(0), world.getCurrentBalance(principal));
        world.addTransaction(principal, transaction);
        assertEquals(1, world.getNumberOfTransactions(principal));
        assertEquals(new Money(100), world.getCurrentBalance(principal));
        Transaction t2 = world.getTransaction(principal, 0);
        assertEquals(transaction, t2);
        Transaction t3 = world.removeLastTransaction(principal);
        assertEquals(transaction, t3);
        assertEquals(new Money(0), world.getCurrentBalance(principal));
    }

}
