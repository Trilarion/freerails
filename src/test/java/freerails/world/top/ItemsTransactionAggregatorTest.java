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
 * Created on 22-Jul-2005
 *
 */
package freerails.world.top;

import freerails.world.accounts.AddItemTransaction;
import freerails.world.accounts.Transaction;
import freerails.world.accounts.Transaction.Category;
import freerails.world.common.Money;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.player.Player;
import junit.framework.TestCase;

import static freerails.world.accounts.Transaction.Category.TRACK;

/**
 *
 */
public class ItemsTransactionAggregatorTest extends TestCase {

    /**
     *
     */
    public void test1() {
        World w = new WorldImpl();
        Player player = new Player("name", 0);
        w.addPlayer(player);
        FreerailsPrincipal fp = w.getPlayer(0).getPrincipal();
        ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(
                w, fp);
        aggregator.setCategory(TRACK);
        int quant = aggregator.calculateQuantity();
        assertEquals(0, quant);
        Transaction t = new AddItemTransaction(Category.TRACK, 10, 5,
                new Money(100));
        w.addTransaction(fp, t);

        quant = aggregator.calculateQuantity();
        assertEquals(5, quant);
        t = new AddItemTransaction(Category.TRACK, 10, 11, new Money(200));
        w.addTransaction(fp, t);

    }

}
