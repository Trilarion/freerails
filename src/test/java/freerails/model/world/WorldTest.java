/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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

import freerails.model.cargo.Cargo;
import freerails.model.cargo.CargoCategory;
import freerails.model.finance.transaction.Transaction;
import freerails.model.finance.transaction.TransactionCategory;
import freerails.util.Vec2D;
import freerails.util.Utils;
import freerails.model.finance.*;
import freerails.model.player.Player;
import freerails.model.track.TrackCategory;
import freerails.model.track.TrackType;
import freerails.util.WorldGenerator;
import junit.framework.TestCase;

import java.io.Serializable;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Collections;

/**
 * Test for World.
 */
public class WorldTest extends TestCase {

    private static final Serializable fs = new TestState(1);

    /**
     *
     */
    public void testConstructor() {
        World world = WorldGenerator.minimalWorld();
        assertEquals(world.getMapSize(), new Vec2D(20, 20));
    }

    /**
     * Tests that changing the object returned by defensiveCopy() does not alter
     * the world object that was copied.
     */
    public void testDefensiveCopy() {
        World original;
        World copy;
        original = WorldGenerator.minimalWorld();
        /**
         * Returns a copy of this world object - making changes to this copy will
         * not change this object.
         */
        copy = (World) Utils.cloneBySerialisation(original);
        assertNotSame("The copies should be different objects.", original, copy);
        assertEquals("The copies should be logically equal.", original, copy);
    }

    /**
     *
     */
    public void testEquals() {
        World original;
        World copy;
        original = WorldGenerator.minimalWorld();
        /**
         * Returns a copy of this world object - making changes to this copy will
         * not change this object.
         */
        copy = (World) Utils.cloneBySerialisation(original);

        Player player = new Player(0, "Name");
        int index = copy.addPlayer(player);
        assertEquals(index, 0);
        assertFalse(copy.equals(original));
        original.addPlayer(player);
        assertEquals("The copies should be logically equal.", original, copy);

        assertTrue(Utils.equalsBySerialization(original, copy));

        Transaction transaction = new Transaction(TransactionCategory.MISC_INCOME, new Money(100), copy.getClock().getCurrentTime());
        copy.addTransaction(player, transaction);
        assertEquals(new Money(100), copy.getCurrentBalance(player));
        assertFalse(copy.equals(original));
    }

    /**
     *
     */
    public void testEquals2() {
        World original;
        World copy, copy2;
        original = WorldGenerator.minimalWorld();
        /**
         * Returns a copy of this world object - making changes to this copy will
         * not change this object.
         */
        copy = (World) Utils.cloneBySerialisation(original);
        /**
         * Returns a copy of this world object - making changes to this copy will
         * not change this object.
         */
        copy2 = (World) Utils.cloneBySerialisation(original);
        // Test adding players.
        Player a = new Player(0, "Fred");
        Player b = new Player(1, "John");
        original.addPlayer(a);
        copy.addPlayer(b);
        assertFalse(copy.equals(original));
    }

    /**
     *
     */
    public void testBoundsContain() {
        World world = WorldGenerator.minimalWorld();
        assertTrue(world.boundsContain(new Vec2D(1, 1)));
        assertTrue(world.boundsContain(Vec2D.ZERO));
        assertFalse(world.boundsContain(new Vec2D(-1, -1)));
        assertFalse(world.boundsContain(new Vec2D(20, 20)));
    }

    /**
     *
     */
    public void testBankAccount() {
        World world = WorldGenerator.minimalWorld();
        Player player = new Player(0, "Test");
        int playerID = world.addPlayer(player);
        assertEquals(0, playerID);
        player = world.getPlayer(playerID);
        Transaction transaction = new Transaction(TransactionCategory.BOND, new Money(100), world.getClock().getCurrentTime());
        assertEquals(new Money(0), world.getCurrentBalance(player));
        world.addTransaction(player, transaction);
        assertEquals(1, world.getTransactions(player).size());
        assertEquals(new Money(100), world.getCurrentBalance(player));
        Transaction t2 = world.getTransaction(player, 0);
        assertEquals(transaction, t2);
        Transaction t3 = world.removeLastTransaction(player);
        assertEquals(transaction, t3);
        assertEquals(new Money(0), world.getCurrentBalance(player));
    }

    public void testSparsePlayerIdsMapToStableCollectionIndices() {
        World world = WorldGenerator.minimalWorld();
        Player first = new Player(40, "First");
        Player second = new Player(7, "Second");

        assertEquals(0, world.addPlayer(first));
        assertEquals(1, world.addPlayer(second));
        assertEquals(0, world.getPlayerIndex(40));
        assertEquals(1, world.getPlayerIndex(7));

        world.addTransaction(first,
                new Transaction(TransactionCategory.MISC_INCOME, new Money(25), world.getClock().getCurrentTime()));
        assertEquals(new Money(25), world.getCurrentBalance(first));

        World copy = (World) Utils.cloneBySerialisation(world);
        assertEquals(0, copy.getPlayerIndex(40));
        assertEquals(new Money(25), copy.getCurrentBalance(first));
    }

    public void testSparseCargoIdsMapToCollectionIndicesAndBuilderCopiesInput() {
        World base = WorldGenerator.minimalWorld();
        SortedSet<Cargo> cargos = new TreeSet<>();
        cargos.add(new Cargo(7, "Mail", CargoCategory.MAIL, 1));
        World.Builder builder = new World.Builder()
                .setRules(base.getRules())
                .setMapSize(new Vec2D(2, 2))
                .setCargos(cargos);
        World world = builder.build();

        cargos.add(new Cargo(90, "Coal", CargoCategory.BULK_FREIGHT, 2));
        assertEquals(1, world.getCargos().size());
        assertEquals(0, world.getCargoIndex(7));
        assertEquals(7, world.getCargoAtIndex(0).getId());

        try {
            world.getCargos().clear();
            fail("World cargo collection must be read-only.");
        } catch (UnsupportedOperationException expected) {
            // Expected.
        }
    }

    public void testSparseTrackTypeIdsMapToCollectionIndices() {
        World base = WorldGenerator.minimalWorld();
        SortedSet<TrackType> trackTypes = new TreeSet<>();
        trackTypes.add(new TrackType(12, "first", TrackCategory.TRACK, Collections.emptySet(),
                Money.ZERO, Money.ZERO, Collections.emptySet(), Collections.emptySet()));
        trackTypes.add(new TrackType(88, "second", TrackCategory.TRACK, Collections.emptySet(),
                Money.ZERO, Money.ZERO, Collections.emptySet(), Collections.emptySet()));
        World world = new World.Builder()
                .setRules(base.getRules())
                .setMapSize(new Vec2D(2, 2))
                .setTrackTypes(trackTypes)
                .build();

        assertEquals(0, world.getTrackTypeIndex(12));
        assertEquals(1, world.getTrackTypeIndex(88));
        assertEquals(88, world.getTrackTypeAtIndex(1).getId());
    }

    public void testMapDimensionAndTileCountLimits() {
        assertInvalidMapSize(new Vec2D(10_001, 1));
        assertInvalidMapSize(new Vec2D(1_000, 1_001));
    }

    private static void assertInvalidMapSize(Vec2D size) {
        try {
            WorldGenerator.minimalWorld(size);
            fail("Expected invalid map size to be rejected: " + size);
        } catch (IllegalArgumentException expected) {
            // Expected.
        }
    }

}
