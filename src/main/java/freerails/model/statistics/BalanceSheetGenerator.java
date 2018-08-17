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

package freerails.model.statistics;

import freerails.model.game.Calendar;
import freerails.model.game.Time;
import freerails.model.player.Player;
import freerails.model.world.UnmodifiableWorld;

/**
 * Generates the balance sheet - note, its fields are read using reflection so
 * don't change their names.
 */
public class BalanceSheetGenerator {

    public final Statistics total;
    public final Statistics ytd;

    /**
     * @param world
     * @param player
     */
    public BalanceSheetGenerator(UnmodifiableWorld world, Player player) {
        Calendar calendar = world.getCalendar();
        // Calculate totals
        Time time = world.currentTime();
        final int startYear = calendar.getYear(time.getTicks());
        Time startOfYear = new Time(calendar.getTicks(startYear));
        Time[] totalTimeInterval = new Time[]{Time.BIG_BANG, Time.DOOMSDAY};
        total = new Statistics(world, player, totalTimeInterval);

        Time[] ytdTimeInterval = new Time[]{startOfYear, Time.DOOMSDAY};
        ytd = new Statistics(world, player, ytdTimeInterval);
    }

}
