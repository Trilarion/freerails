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
package freerails.server;

import freerails.world.ItemsTransactionAggregator;
import freerails.world.World;
import freerails.world.WorldImpl;
import freerails.world.finances.ItemTransaction;
import freerails.world.finances.Money;
import freerails.world.finances.TransactionCategory;
import freerails.world.top.MapFixtureFactory;
import junit.framework.TestCase;

import java.util.Arrays;

/**
 * Test for TrackMaintenanceMoveGenerator.
 */
public class TrackMaintenanceMoveGeneratorTest extends TestCase {

    private World world;

    /**
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        world = new WorldImpl(20, 20);
        world.addPlayer(MapFixtureFactory.TEST_PLAYER);
        MapFixtureFactory.generateTrackRuleList(world);
    }

    /**
     *
     */
    public void testCalulateNumberOfEachTrackType() {
        int[] actual;
        int[] expected;
        actual = calNumOfEachTrackType();

        /*
         * actual = ItemsTransactionAggregator.calculateNumberOfEachTrackType(world,
         * MapFixtureFactory.TEST_PRINCIPAL, 0);
         */
        expected = new int[]{0, 0, 0}; // No track has been built yet.
        assertTrue(Arrays.equals(expected, actual));

        addTrack(0, 10);

        actual = calNumOfEachTrackType();
        expected = new int[]{10, 0, 0};
        assertTrue(Arrays.equals(expected, actual));

        addTrack(2, 20);

        actual = calNumOfEachTrackType();
        expected = new int[]{10, 0, 20};
        assertTrue(Arrays.equals(expected, actual));
    }

    private int[] calNumOfEachTrackType() {
        int[] actual;
        ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(
                world, MapFixtureFactory.TEST_PRINCIPAL);
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
        ItemTransaction t = new ItemTransaction(
                TransactionCategory.TRACK, trackType, quantity, new Money(
                trackType));
        world.addTransaction(MapFixtureFactory.TEST_PRINCIPAL, t);
    }
}