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

public class ItemsTransactionAggregatorTest extends TestCase {

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
