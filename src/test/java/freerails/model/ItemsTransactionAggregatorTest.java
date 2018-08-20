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
package freerails.model;

import freerails.model.finance.*;
import freerails.model.finance.transaction.ItemTransaction;
import freerails.model.finance.transaction.Transaction;
import freerails.model.finance.transaction.TransactionCategory;
import freerails.model.finance.transaction.aggregator.ItemsTransactionAggregator;
import freerails.model.game.Time;
import freerails.model.player.Player;
import freerails.model.world.World;
import freerails.util.WorldGenerator;
import junit.framework.TestCase;

/**
 *
 */
public class ItemsTransactionAggregatorTest extends TestCase {

    /**
     *
     */
    public void test1() {
        World world = WorldGenerator.minimalWorld();
        Player player = new Player(0, "name");
        world.addPlayer(player);
        player = world.getPlayer(0);
        Time[] times = {Time.ZERO, world.getClock().getCurrentTime().advance()};
        ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(world, player, times);
        aggregator.setCategory(TransactionCategory.TRACK);
        int quantity = aggregator.calculateQuantity();
        assertEquals(0, quantity);
        Transaction transaction = new ItemTransaction(TransactionCategory.TRACK, new Money(100), world.getClock().getCurrentTime(), 5, 10);
        world.addTransaction(player, transaction);

        quantity = aggregator.calculateQuantity();
        assertEquals(5, quantity);
        transaction = new ItemTransaction(TransactionCategory.TRACK, new Money(200), world.getClock().getCurrentTime(), 11, 10);
        world.addTransaction(player, transaction);
    }

}
