/*
 * Created on Sep 5, 2004
 *
 */
package jfreerails.client.view;

import jfreerails.world.accounts.BondTransaction;
import jfreerails.world.common.GameCalendar;
import jfreerails.world.common.GameTime;
import jfreerails.world.common.Money;
import jfreerails.world.player.Player;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.MapFixtureFactory;
import jfreerails.world.top.World;
import jfreerails.world.top.WorldImpl;
import junit.framework.TestCase;

/**JUnit test for BalanceSheetGenerator.
 * @author Luke
 *
 */
public class BalanceSheetGeneratorTest extends TestCase {

    public void test1(){
        Player player = MapFixtureFactory.TEST_PLAYER;     
        World world = new WorldImpl(10, 10);
        world.set(ITEM.CALENDAR, new GameCalendar(1200, 1840));
        world.set(ITEM.TIME, new GameTime(0));
        world.addPlayer(player);
        world.addTransaction(BondTransaction.issueBond(5), player.getPrincipal());
        world.addTransaction(BondTransaction.issueBond(5), player.getPrincipal());
        world.set(ITEM.TIME, new GameTime(100));
        
        BalanceSheetGenerator generator = new BalanceSheetGenerator(world, player.getPrincipal());
        Money expectedBondValue = new Money(BondTransaction.BOND_VALUE.getAmount() * 2);
        assertEquals(expectedBondValue.changeSign(),  generator.loansTotal);
        assertEquals(expectedBondValue.changeSign(),  generator.loansYtd);
        
    }
    
}
