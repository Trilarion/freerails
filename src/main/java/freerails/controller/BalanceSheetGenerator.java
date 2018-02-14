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
import freerails.world.game.GameCalendar;
import freerails.world.game.GameTime;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.world.ReadOnlyWorld;

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

}
