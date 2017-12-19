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
 * Created on 11-Aug-2003
 *
 */
package freerails.server;

import freerails.world.accounts.AddItemTransaction;
import freerails.world.accounts.Transaction;
import freerails.world.common.Money;
import freerails.world.top.ItemsTransactionAggregator;
import freerails.world.top.MapFixtureFactory;
import freerails.world.top.World;
import freerails.world.top.WorldImpl;
import junit.framework.TestCase;

import java.util.Arrays;

/**
 * JUnit test for TrackMaintenanceMoveGenerator.
 *
 */
public class TrackMaintenanceMoveGeneratorTest extends TestCase {
    private World w;

    /**
     *
     * @throws Exception
     */
    @Override
    protected void setUp() throws Exception {
        w = new WorldImpl(20, 20);
        w.addPlayer(MapFixtureFactory.TEST_PLAYER);
        MapFixtureFactory.generateTrackRuleList(w);
    }

    /**
     *
     */
    public void testCalulateNumberOfEachTrackType() {
        int[] actual;
        int[] expected;
        actual = calNumOfEachTrackType();

        /*
         * actual = ItemsTransactionAggregator.calulateNumberOfEachTrackType(w,
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
                w, MapFixtureFactory.TEST_PRINCIPAL);
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
     * Utility method to add the specifed number of units of the specified track
     * type.
     */
    private void addTrack(int trackType, int quantity) {
        AddItemTransaction t = new AddItemTransaction(
                Transaction.Category.TRACK, trackType, quantity, new Money(
                trackType));
        w.addTransaction(MapFixtureFactory.TEST_PRINCIPAL, t);
    }
}