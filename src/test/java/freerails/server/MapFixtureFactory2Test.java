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

package freerails.server;

import freerails.world.SKEY;
import freerails.world.World;
import freerails.world.finances.ItemTransaction;
import freerails.world.finances.Money;
import freerails.world.finances.Transaction;
import freerails.world.finances.TransactionCategory;
import freerails.world.player.FreerailsPrincipal;
import junit.framework.TestCase;

/**
 */
public class MapFixtureFactory2Test extends TestCase {

    private World world;

    /**
     *
     */
    public void testGetCopy() {
        World w2;
        world = MapFixtureFactory2.getCopy();
        assertNotNull(world);
        w2 = MapFixtureFactory2.getCopy();
        assertNotNull(w2);
        assertNotSame(world, w2);
        assertEquals(world, w2);

    }

    /**
     *
     */
    public void testLists() {

        assertTrue(world.size(SKEY.CARGO_TYPES) > 0);
        assertTrue(world.size(SKEY.TRACK_RULES) > 0);
        assertTrue(world.size(SKEY.TERRAIN_TYPES) > 0);

    }

    /**
     *
     */
    public void testMap() {

        assertEquals(world.getMapWidth(), 50);
        assertEquals(world.getMapWidth(), 50);

    }

    /**
     *
     */
    public void testPlayers() {

        assertEquals(4, world.getNumberOfPlayers());
    }

    /**
     *
     */
    public void testThatStockIsIssued() {
        FreerailsPrincipal p = world.getPlayer(0).getPrincipal();
        int stock = 0;
        Money cash = world.getCurrentBalance(p);
        assertEquals(new Money(1000000), cash);
        int numberOfTransactions = world.getNumberOfTransactions(p);
        assertTrue(numberOfTransactions > 0);
        for (int i = 0; i < numberOfTransactions; i++) {
            Transaction t = world.getTransaction(p, i);
            if (t.getCategory() == TransactionCategory.ISSUE_STOCK) {
                ItemTransaction ait = (ItemTransaction) t;
                stock += ait.getQuantity();
            }
        }
        assertEquals(100000, stock);
    }

    /**
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        world = MapFixtureFactory2.getCopy();
    }

}
