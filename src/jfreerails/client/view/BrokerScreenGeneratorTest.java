/*
 * Created on 05-Dec-2005
 * 
 */
package jfreerails.client.view;

import jfreerails.controller.StockPriceCalculator;
import jfreerails.controller.StockPriceCalculator.StockPrice;
import jfreerails.move.AddPlayerMove;
import jfreerails.move.AddTransactionMove;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.world.accounts.StockTransaction;
import jfreerails.world.common.GameCalendar;
import jfreerails.world.common.Money;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.player.Player;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.MapFixtureFactory;
import jfreerails.world.top.World;
import jfreerails.world.top.WorldImpl;
import junit.framework.TestCase;

public class BrokerScreenGeneratorTest extends TestCase {

	private World world;

	int playerID;

	FreerailsPrincipal principal;

	@Override
	protected void setUp() throws Exception {
		// TODO Auto-generated method stub
		super.setUp();
		world = new WorldImpl(10, 10);
		// Set the time..
		world.set(ITEM.CALENDAR, new GameCalendar(12000, 1840));
		Player p = MapFixtureFactory.TEST_PLAYER;

		AddPlayerMove apm = AddPlayerMove.generateMove(world, p);
		MoveStatus ms = apm.doMove(world, Player.AUTHORITATIVE);
		assertTrue(ms.isOk());
		playerID = world.getNumberOfPlayers() - 1;
		principal = p.getPrincipal();
	}

	/**
	 * Testcase to reproduce bug [ 1341365 ] Exception when calculating stock
	 * price after buying shares
	 */
	public void testBuyingStock() {

		for (int i = 0; i < 9; i++) {
			StockPrice stockPrice = new StockPriceCalculator(world).calculate()[playerID];
			Money sharePrice = stockPrice.treasuryBuyPrice;
			StockTransaction t = StockTransaction.buyOrSellStock(playerID,
					StockTransaction.STOCK_BUNDLE_SIZE, sharePrice);
			Move move = new AddTransactionMove(principal, t);
			MoveStatus ms = move.doMove(world, Player.AUTHORITATIVE);
			assertTrue(ms.isOk());
			//The line below threw an exception that caused bug 1341365.
			BrokerScreenGenerator brokerScreenGenerator = new BrokerScreenGenerator(world, principal);
			assertNotNull(brokerScreenGenerator);
		}

	}

}
