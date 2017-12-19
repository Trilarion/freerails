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
 * Created on 05-Dec-2005
 *
 */
package freerails.client.view;

import freerails.controller.StockPriceCalculator;
import freerails.controller.StockPriceCalculator.StockPrice;
import freerails.move.AddPlayerMove;
import freerails.move.AddTransactionMove;
import freerails.move.Move;
import freerails.move.MoveStatus;
import freerails.world.common.GameCalendar;
import freerails.world.finances.Money;
import freerails.world.finances.StockTransaction;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.player.Player;
import freerails.world.top.ITEM;
import freerails.world.top.MapFixtureFactory;
import freerails.world.top.World;
import freerails.world.top.WorldImpl;
import junit.framework.TestCase;

/**
 *
 */
public class BrokerScreenGeneratorTest extends TestCase {

    int playerID;
    FreerailsPrincipal principal;
    private World world;

    /**
     * @throws Exception
     */
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
            // The line below threw an exception that caused bug 1341365.
            BrokerScreenGenerator brokerScreenGenerator = new BrokerScreenGenerator(
                    world, principal);
            assertNotNull(brokerScreenGenerator);
        }

    }

}
