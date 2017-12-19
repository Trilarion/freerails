/*
 * Created on Sep 5, 2004
 *
 */
package freerails.client.view;

import freerails.controller.BalanceSheetGenerator;
import freerails.move.AddPlayerMove;
import freerails.move.Move;
import freerails.move.MoveStatus;
import freerails.world.accounts.BondTransaction;
import freerails.world.common.GameCalendar;
import freerails.world.common.GameTime;
import freerails.world.common.Money;
import freerails.world.player.Player;
import freerails.world.top.ITEM;
import freerails.world.top.World;
import freerails.world.top.WorldImpl;
import junit.framework.TestCase;

/**
 * JUnit test for BalanceSheetGenerator.
 *
 */
public class BalanceSheetGeneratorTest extends TestCase {

    Player player;

    World world;

    /**
     *
     */
    public void testBondsFigure() {

        BalanceSheetGenerator generator = new BalanceSheetGenerator(world,
                player.getPrincipal());
        Money expectedBondValue = new Money(BondTransaction.BOND_VALUE_ISSUE
                .getAmount());
        assertEquals(expectedBondValue.changeSign(), generator.total.loans);
        assertEquals(expectedBondValue.changeSign(), generator.ytd.loans);
    }

    /**
     *
     */
    public void testStochHolderEquityFigure() {

        BalanceSheetGenerator generator = new BalanceSheetGenerator(world,
                player.getPrincipal());

        Money expectStockHolderEquity = new Money(-500000);

        assertEquals(expectStockHolderEquity, generator.total.equity);

    }

    /**
     *
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {

        world = new WorldImpl(10, 10);
        player = new Player("Player X", world.getNumberOfPlayers());
        world.set(ITEM.CALENDAR, new GameCalendar(1200, 1840));
        world.setTime(new GameTime(0));

        Move addPlayerMove = AddPlayerMove.generateMove(world, player);
        MoveStatus ms = addPlayerMove.doMove(world, player.getPrincipal());
        assertTrue(ms.message, ms.ok);

        world.setTime(new GameTime(100));
    }

}
