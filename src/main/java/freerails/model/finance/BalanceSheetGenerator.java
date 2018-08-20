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

package freerails.model.finance;

import freerails.model.game.Clock;
import freerails.model.game.Time;
import freerails.model.player.Player;
import freerails.model.world.UnmodifiableWorld;

// TODO make code static instead
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
        // Calculate totals
        Clock clock = world.getClock();
        Time[] totalTimeInterval = new Time[]{Time.ZERO, world.getClock().getCurrentTime().advance()};
        total = new Statistics(world, player, totalTimeInterval);

        Time[] ytdTimeInterval = new Time[]{clock.getTimeAtStartOfCurrentYear(), world.getClock().getCurrentTime().advance()};
        ytd = new Statistics(world, player, ytdTimeInterval);
    }

}
