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

package freerails.model.economy;

import freerails.model.economy.transaction.ItemTransaction;
import freerails.model.economy.transaction.TransactionCategory;
import freerails.model.player.Player;
import freerails.model.simulation.Time;
import freerails.model.track.TrackCategory;
import freerails.model.track.TrackProperty;
import freerails.model.track.TrackType;
import freerails.model.world.World;
import freerails.util.IntPoint2D;
import freerails.util.TestUtils;
import freerails.util.WorldGenerator;
import junit.framework.TestCase;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/** Tests financial transaction helper methods. */
public class TransactionUtilsTest extends TestCase {

    /**
     * Tests track value uses the purchase price, quantity, and length conversion.
     */
    public void testCalculateTrackTotalUsesMoneyArithmetic() {
        World world = createWorld(new Money(1_234));
        Player player = new Player(0, "test");
        world.addPlayer(player);
        world.addTransaction(player, new ItemTransaction(
                TransactionCategory.TRACK, Money.ZERO, Time.ZERO, 250, 0));

        Money trackTotal = TransactionUtils.calculateTrackTotal(world, player, Time.ZERO);

        assertEquals(new Money(1_542), trackTotal);
    }

    /**
     * Tests track valuation rejects overflow instead of wrapping a long.
     */
    public void testCalculateTrackTotalRejectsOverflow() {
        World world = createWorld(new Money(Long.MAX_VALUE));
        Player player = new Player(0, "test");
        world.addPlayer(player);
        world.addTransaction(player, new ItemTransaction(
                TransactionCategory.TRACK, Money.ZERO, Time.ZERO, 2, 0));

        TestUtils.assertThrows(ArithmeticException.class,
                () -> TransactionUtils.calculateTrackTotal(world, player, Time.ZERO));
    }

    /**
     * Creates a minimal world with a single track type at the supplied price.
     *
     * @param purchasingPrice purchase price assigned to the track type
     * @return world containing the priced track type
     */
    private static World createWorld(Money purchasingPrice) {
        World base = WorldGenerator.testWorld(true);
        TrackType trackType = new TrackType(0, "test-track", TrackCategory.TRACK,
                Set.of(TrackProperty.SINGLE), Money.ZERO, purchasingPrice,
                Set.of(), Set.of());
        SortedSet<TrackType> trackTypes = new TreeSet<>();
        trackTypes.add(trackType);

        return new World.Builder()
                .setRules(base.getRules())
                .setTrackTypes(trackTypes)
                .setTerrainTypes(new TreeSet<>(base.getTerrains()))
                .setMapSize(new IntPoint2D(1, 1))
                .build();
    }
}
