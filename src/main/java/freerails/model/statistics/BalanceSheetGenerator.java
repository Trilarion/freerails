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

import freerails.model.game.GameCalendar;
import freerails.model.game.GameTime;
import freerails.model.player.FreerailsPrincipal;
import freerails.model.world.WorldItem;
import freerails.model.world.ReadOnlyWorld;

/**
 * Generates the balance sheet - note, its fields are read using reflection so
 * don't change their names.
 */
public class BalanceSheetGenerator {

    public final Statistics total;
    public final Statistics ytd;

    /**
     * @param world
     * @param principal
     */
    public BalanceSheetGenerator(ReadOnlyWorld world, FreerailsPrincipal principal) {
        GameCalendar calendar = (GameCalendar) world.get(WorldItem.Calendar);
        // Calculate totals
        GameTime time = world.currentTime();
        final int startYear = calendar.getYear(time.getTicks());
        GameTime startOfYear = new GameTime(calendar.getTicks(startYear));
        GameTime[] totalTimeInterval = new GameTime[]{GameTime.BIG_BANG, GameTime.DOOMSDAY};
        total = new Statistics(world, principal, totalTimeInterval);

        GameTime[] ytdTimeInterval = new GameTime[]{startOfYear, GameTime.DOOMSDAY};
        ytd = new Statistics(world, principal, ytdTimeInterval);
    }

}
