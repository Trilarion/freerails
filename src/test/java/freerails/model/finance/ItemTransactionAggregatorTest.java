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

    public void testFinanceIntervalsIncludeStartExcludeEndAndKeepZeroPeriods() {
        addTransaction(4, TransactionCategory.TRACK, 100, 10, 0);
        addTransaction(5, TransactionCategory.TRACK, 10, 1, 0);
        addTransaction(9, TransactionCategory.TRACK, 20, 2, 0);
        addTransaction(9, TransactionCategory.TRAIN, 100, 1, 0);
        addTransaction(10, TransactionCategory.TRACK, 30, 3, 0);
        addTransaction(14, TransactionCategory.TRAIN, 40, 4, 0);
        addTransaction(15, TransactionCategory.TRACK, 50, 5, 0);

        Time[] times = {new Time(5), new Time(10), new Time(12), new Time(15)};
        ItemsTransactionAggregator trackAggregator = new ItemsTransactionAggregator(world, WorldGenerator.TEST_PLAYER, times);
        trackAggregator.setCategory(TransactionCategory.TRACK);
        trackAggregator.setType(0);
        trackAggregator.aggregate();
        Money[] expectedTrackValues = {new Money(30), new Money(30), Money.ZERO};
        assertTrue(Arrays.equals(expectedTrackValues, trackAggregator.getValues()));
        assertEquals(3, trackAggregator.calculateQuantity());

        ItemsTransactionAggregator trainAggregator = new ItemsTransactionAggregator(world, WorldGenerator.TEST_PLAYER, times);
        trainAggregator.setCategory(TransactionCategory.TRAIN);
        trainAggregator.setType(0);
        trainAggregator.aggregate();
        Money[] expectedTrainValues = {new Money(100), Money.ZERO, new Money(40)};
        assertTrue(Arrays.equals(expectedTrainValues, trainAggregator.getValues()));
    }

    public void testOutOfOrderHistoryIsKeptInInsertionOrderAndAggregatedByTimestamp() {
        ItemTransaction later = itemTransaction(8, 10, 0);
        ItemTransaction earlier = itemTransaction(2, 20, 0);
        ItemTransaction sameTimeFirst = itemTransaction(5, 30, 0);
        ItemTransaction sameTimeSecond = itemTransaction(5, 40, 0);
        world.addTransaction(WorldGenerator.TEST_PLAYER, later);
        world.addTransaction(WorldGenerator.TEST_PLAYER, earlier);
        world.addTransaction(WorldGenerator.TEST_PLAYER, sameTimeFirst);
        world.addTransaction(WorldGenerator.TEST_PLAYER, sameTimeSecond);

        assertEquals(4, world.getTransactions(WorldGenerator.TEST_PLAYER).size());
        assertEquals(later, world.getTransaction(WorldGenerator.TEST_PLAYER, 0));
        assertEquals(earlier, world.getTransaction(WorldGenerator.TEST_PLAYER, 1));
        assertEquals(sameTimeFirst, world.getTransaction(WorldGenerator.TEST_PLAYER, 2));
        assertEquals(sameTimeSecond, world.getTransaction(WorldGenerator.TEST_PLAYER, 3));

        ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(world,
                WorldGenerator.TEST_PLAYER, new Time[]{Time.ZERO, new Time(4), new Time(7), new Time(10)});
        aggregator.setCategory(TransactionCategory.TRACK);
        aggregator.setType(0);
        aggregator.aggregate();
        assertTrue(Arrays.equals(new Money[]{new Money(20), new Money(70), new Money(10)},
                aggregator.getValues()));
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

    private void addTransaction(int time, TransactionCategory category, long amount, int quantity, int id) {
        world.addTransaction(WorldGenerator.TEST_PLAYER,
                new ItemTransaction(category, new Money(amount), new Time(time), quantity, id));
    }

    private static ItemTransaction itemTransaction(int time, long amount, int id) {
        return new ItemTransaction(TransactionCategory.TRACK, new Money(amount), new Time(time), 1, id);
    }
}