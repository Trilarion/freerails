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
package freerails.model.finance;

import freerails.model.finance.transaction.ItemTransaction;
import freerails.model.finance.transaction.TransactionCategory;
import freerails.model.finance.transaction.aggregator.ItemsTransactionAggregator;
import freerails.model.game.Time;
import freerails.model.world.World;
import freerails.util.WorldGenerator;
import junit.framework.TestCase;

import java.util.Arrays;

/**
 * Test for ItemTransactionAggregator.
 */
public class ItemTransactionAggregatorTest extends TestCase {

    private World world;

    /**
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        world = WorldGenerator.testWorld(true);
        world.addPlayer(WorldGenerator.TEST_PLAYER);
    }

    /**
     *
     */
    public void testCalculateNumberOfEachTrackType() {
        int[] actual;
        int[] expected;
        actual = calculateNumOfEachTrackType();

        /*
         * actual = ItemsTransactionAggregator.calculateNumberOfEachTrackType(world,
         * MapFixtureFactory.TEST_PLAYER, 0);
         */
        expected = new int[]{0, 0, 0}; // No track has been built yet.
        assertTrue(Arrays.equals(expected, actual));

        addTrack(0, 10);

        actual = calculateNumOfEachTrackType();
        expected = new int[]{10, 0, 0};
        assertTrue(Arrays.equals(expected, actual));

        addTrack(2, 20);

        actual = calculateNumOfEachTrackType();
        expected = new int[]{10, 0, 20};
        assertTrue(Arrays.equals(expected, actual));
    }

    /**
     *
     * @return
     */
    private int[] calculateNumOfEachTrackType() {
        int[] actual;
        Time[] times = {Time.ZERO, world.getClock().getCurrentTime().advance()};
        ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(world, WorldGenerator.TEST_PLAYER, times);
        actual = new int[3];
        aggregator.setType(0);
        actual[0] = aggregator.calculateQuantity();
        aggregator.setType(1);
        actual[1] = aggregator.calculateQuantity();
        aggregator.setType(2);
        actual[2] = aggregator.calculateQuantity();

        return actual;
    }

    /**
     * Utility method to add the specified number of units of the specified track
     * type.
     */
    private void addTrack(int trackType, int quantity) {
        ItemTransaction transaction = new ItemTransaction(TransactionCategory.TRACK, new Money(trackType), world.getClock().getCurrentTime(), quantity, trackType);
        world.addTransaction(WorldGenerator.TEST_PLAYER, transaction);
    }
}