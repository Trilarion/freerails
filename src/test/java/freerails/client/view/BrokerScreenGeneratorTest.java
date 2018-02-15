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
package freerails.client.view;

import freerails.model.finances.StockPriceCalculator;
import freerails.model.finances.StockPrice;
import freerails.model.world.ITEM;
import freerails.move.AddPlayerMove;
import freerails.move.AddTransactionMove;
import freerails.move.Move;
import freerails.move.MoveStatus;
import freerails.model.*;
import freerails.model.finances.Money;
import freerails.model.finances.StockItemTransaction;
import freerails.model.game.GameCalendar;
import freerails.model.player.FreerailsPrincipal;
import freerails.model.player.Player;
import freerails.model.world.FullWorld;
import freerails.model.world.World;
import junit.framework.TestCase;

/**
 *
 */
public class BrokerScreenGeneratorTest extends TestCase {

    private int playerID;
    private FreerailsPrincipal principal;
    private World world;

    /**
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        world = new FullWorld(10, 10);
        // Set the time..
        world.set(ITEM.CALENDAR, new GameCalendar(12000, 1840));
        Player player = MapFixtureFactory.TEST_PLAYER;

        AddPlayerMove apm = AddPlayerMove.generateMove(world, player);
        MoveStatus moveStatus = apm.doMove(world, Player.AUTHORITATIVE);
        assertTrue(moveStatus.succeeds());
        playerID = world.getNumberOfPlayers() - 1;
        principal = player.getPrincipal();
    }

    /**
     * Testcase to reproduce bug [ 1341365 ] Exception when calculating stock
     * price after buying shares
     */
    public void testBuyingStock() {

        for (int i = 0; i < 9; i++) {
            StockPrice stockPrice = new StockPriceCalculator(world).calculate()[playerID];
            Money sharePrice = stockPrice.treasuryBuyPrice;
            StockItemTransaction stockItemTransaction = StockItemTransaction.buyOrSellStock(playerID,
                    WorldConstants.STOCK_BUNDLE_SIZE, sharePrice);
            Move move = new AddTransactionMove(principal, stockItemTransaction);
            MoveStatus moveStatus = move.doMove(world, Player.AUTHORITATIVE);
            assertTrue(moveStatus.succeeds());
            // The line below threw an exception that caused bug 1341365.
            BrokerScreenGenerator brokerScreenGenerator = new BrokerScreenGenerator(world, principal);
            assertNotNull(brokerScreenGenerator);
        }
    }

}
