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

import freerails.model.finance.transaction.ItemTransaction;
import freerails.model.finance.Money;
import freerails.model.finance.transaction.Transaction;
import freerails.model.finance.transaction.TransactionCategory;
import freerails.model.player.Player;
import freerails.model.world.World;
import freerails.util.Vec2D;
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
        World world2;
        world = MapFixtureFactory2.getCopy();
        assertNotNull(world);
        world2 = MapFixtureFactory2.getCopy();
        assertNotNull(world2);
        assertNotSame(world, world2);
        assertEquals(world, world2);
    }

    /**
     *
     */
    public void testLists() {

        assertTrue(world.getCargos().size() > 0);
        assertTrue(world.getTrackTypes().size() > 0);
        assertTrue(world.getTerrains().size() > 0);
    }

    /**
     *
     */
    public void testMap() {
        assertEquals(world.getMapSize(), new Vec2D(200, 200));
    }

    /**
     *
     */
    public void testPlayers() {
        assertEquals(4, world.getPlayers().size());
    }

    /**
     *
     */
    public void testThatStockIsIssued() {
        Player player = world.getPlayer(0);
        int stock = 0;
        Money cash = world.getCurrentBalance(player);
        assertEquals(new Money(1000000), cash);
        int numberOfTransactions = world.getTransactions(player).size();
        assertTrue(numberOfTransactions > 0);
        for (int i = 0; i < numberOfTransactions; i++) {
            Transaction transaction = world.getTransaction(player, i);
            if (transaction.getCategory() == TransactionCategory.ISSUE_STOCK) {
                ItemTransaction ait = (ItemTransaction) transaction;
                stock += ait.getQuantity();
            }
        }
        assertEquals(100000, stock);
    }
}
