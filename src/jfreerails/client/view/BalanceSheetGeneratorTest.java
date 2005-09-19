/*
 * Created on Sep 5, 2004
 *
 */
package jfreerails.client.view;

import jfreerails.controller.BalanceSheetGenerator;
import jfreerails.move.AddPlayerMove;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.world.accounts.BondTransaction;
import jfreerails.world.common.GameCalendar;
import jfreerails.world.common.GameTime;
import jfreerails.world.common.Money;
import jfreerails.world.player.Player;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.World;
import jfreerails.world.top.WorldImpl;
import junit.framework.TestCase;

/**
 * JUnit test for BalanceSheetGenerator.
 * 
 * @author Luke
 * 
 */
public class BalanceSheetGeneratorTest extends TestCase {

	Player player;

	World world;

	public void testBondsFigure() {

		BalanceSheetGenerator generator = new BalanceSheetGenerator(world,
				player.getPrincipal());
		Money expectedBondValue = new Money(BondTransaction.BOND_VALUE_ISSUE
				.getAmount());
		assertEquals(expectedBondValue.changeSign(), generator.total.loans);
		assertEquals(expectedBondValue.changeSign(), generator.ytd.loans);
	}

	public void testStochHolderEquityFigure() {

		BalanceSheetGenerator generator = new BalanceSheetGenerator(world,
				player.getPrincipal());

		Money expectStockHolderEquity = new Money(-500000);
		
		assertEquals(expectStockHolderEquity, generator.total.equity);

	}

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
