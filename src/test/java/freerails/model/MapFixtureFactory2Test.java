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

package freerails.model;

import freerails.model.finances.ItemTransaction;
import freerails.model.finances.Money;
import freerails.model.finances.Transaction;
import freerails.model.finances.TransactionCategory;
import freerails.model.player.FreerailsPrincipal;
import freerails.model.world.WorldSharedKey;
import freerails.model.world.World;
import junit.framework.TestCase;

/**
 */
public class MapFixtureFactory2Test extends TestCase {

    private World world;

    /**
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        world = MapFixtureFactory2.getCopy();
    }

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

        assertTrue(world.size(WorldSharedKey.CargoTypes) > 0);
        assertTrue(world.size(WorldSharedKey.TrackRules) > 0);
        assertTrue(world.size(WorldSharedKey.TerrainTypes) > 0);
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
        FreerailsPrincipal principal = world.getPlayer(0).getPrincipal();
        int stock = 0;
        Money cash = world.getCurrentBalance(principal);
        assertEquals(new Money(1000000), cash);
        int numberOfTransactions = world.getNumberOfTransactions(principal);
        assertTrue(numberOfTransactions > 0);
        for (int i = 0; i < numberOfTransactions; i++) {
            Transaction transaction = world.getTransaction(principal, i);
            if (transaction.getCategory() == TransactionCategory.ISSUE_STOCK) {
                ItemTransaction ait = (ItemTransaction) transaction;
                stock += ait.getQuantity();
            }
        }
        assertEquals(100000, stock);
    }
}
