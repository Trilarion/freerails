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

package freerails.controller;

import freerails.world.*;
import freerails.world.finances.Money;
import freerails.world.finances.TransactionCategory;
import freerails.world.game.GameCalendar;
import freerails.world.game.GameTime;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.track.TrackRule;

/**
 * Generates the balance sheet - note, its fields are read using reflection so
 * don't change their names.
 */
public class BalanceSheetGenerator {

    public final Stats total;
    public final Stats ytd;

    /**
     * @param world
     * @param principal
     */
    public BalanceSheetGenerator(ReadOnlyWorld world, FreerailsPrincipal principal) {
        GameCalendar cal = (GameCalendar) world.get(ITEM.CALENDAR);
        // Calculate totals
        GameTime time = world.currentTime();
        final int startYear = cal.getYear(time.getTicks());
        GameTime startOfYear = new GameTime(cal.getTicks(startYear));
        GameTime[] totalTimeInterval = new GameTime[]{GameTime.BIG_BANG, GameTime.DOOMSDAY};
        total = new Stats(world, principal, totalTimeInterval);

        GameTime[] ytdTimeInterval = new GameTime[]{startOfYear, GameTime.DOOMSDAY};
        ytd = new Stats(world, principal, ytdTimeInterval);

    }

    /**
     * @param world
     * @param principal
     * @param startTime
     * @return
     */
    public static Money calTrackTotal(ReadOnlyWorld world, FreerailsPrincipal principal, GameTime startTime) {

        ItemsTransactionAggregator aggregator = new ItemsTransactionAggregator(world, principal);
        aggregator.setCategory(TransactionCategory.TRACK);
        long amount = 0;

        for (int i = 0; i < world.size(SKEY.TRACK_RULES); i++) {
            TrackRule trackRule = (TrackRule) world.get(SKEY.TRACK_RULES, i);
            long trackValue = trackRule.getPrice().getAmount();

            GameTime[] times = new GameTime[]{startTime, GameTime.DOOMSDAY};

            aggregator.setType(i);
            aggregator.setTimes(times);
            ItemsTransactionAggregator.QuantitiesAndValues qnv = aggregator.calculateQuantitiesAndValues();
            int quantity = qnv.quantities[0];
            amount += trackValue * quantity / WorldConstants.LENGTH_OF_STRAIGHT_TRACK_PIECE;

        }

        return new Money(amount);
    }

}
