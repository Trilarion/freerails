/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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

import freerails.model.economy.*;
import freerails.model.economy.transaction.ItemTransaction;
import freerails.move.AddPlayerMove;
import freerails.move.AddTransactionMove;
import freerails.move.Move;
import freerails.move.Status;
import freerails.model.*;
import freerails.model.player.Player;
import freerails.model.world.World;
import freerails.util.WorldGenerator;
import junit.framework.TestCase;

import java.net.URL;
import java.util.NoSuchElementException;

/**
 * Tests broker screen generator behavior.
 */
public class BrokerScreenGeneratorTest extends TestCase {

    private int playerID;
    private Player player;
    private World world;

    /**
     * Initializes the fixture before each test.
     * @throws Exception if the set up operation fails.
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        world = WorldGenerator.minimalWorld();
        // Set the time..
        Player player = WorldGenerator.TEST_PLAYER;

        // add a new player
        AddPlayerMove move = new AddPlayerMove(player);
        Status status = move.applicable(world);
        assertTrue(status.isSuccess());
        move.apply(world);
        playerID = world.getPlayers().size() - 1;
        this.player = world.getPlayer(playerID);
    }

    /**
     * Testcase to reproduce bug [ 1341365 ] Exception when calculating stock
     * price after buying shares
     */
    public void testBuyingStock() {

        for (int i = 0; i < 9; i++) {
            StockPrice stockPrice = new StockPriceCalculator(world).calculate()[playerID];
            Money sharePrice = stockPrice.treasuryBuyPrice;
            ItemTransaction stockItemTransaction = TransactionUtils.buyOrSellStock(playerID, ModelConstants.STOCK_BUNDLE_SIZE, sharePrice, world.getClock().getCurrentTime());
            Move move = new AddTransactionMove(player, stockItemTransaction);
            Status status = move.applicable(world);
            assertTrue(status.isSuccess());
            move.apply(world);
            // The line below threw an exception that caused bug 1341365.
            BrokerScreenGenerator brokerScreenGenerator = new BrokerScreenGenerator(world, player);
            assertNotNull(brokerScreenGenerator);
        }
    }

    /**
     * Tests that all broker template tokens are populated from the generator's map.
     */
    public void testBrokerTemplateTokens() {
        BrokerScreenGenerator generator = new BrokerScreenGenerator(world, player);
        URL templateUrl = BrokerScreenHtmlFrame.class.getResource(
                freerails.client.ClientConstants.VIEW_BROKER);
        String template = BrokerFrame.loadText(templateUrl);

        String populatedTemplate = BrokerFrame.populateTokens(template, generator.getTemplateValues());

        assertTrue(populatedTemplate.contains(player.getName()));
        assertFalse(populatedTemplate.contains("$"));
        assertEquals(player.getName(), BrokerFrame.populateTokens(
                "$playername$", generator.getTemplateValues()));
    }

    /**
     * Tests that a template token missing from the map is reported.
     */
    public void testMissingBrokerTemplateToken() {
        try {
            BrokerFrame.populateTokens("$missing$", java.util.Map.of());
            fail("Expected a missing token to be reported.");
        } catch (NoSuchElementException expected) {
            assertEquals("missing", expected.getMessage());
        }
    }

}
