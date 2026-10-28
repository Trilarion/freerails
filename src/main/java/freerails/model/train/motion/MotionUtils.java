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

package freerails.model.train.motion;

import freerails.model.train.activity.Activity;
import freerails.util.BidirectionalIterator;
import freerails.model.player.Player;
import freerails.model.world.UnmodifiableWorld;

/** Utility methods for inspecting train motion histories. */
public final class MotionUtils {

    /** Prevents construction of this utility class. */
    private MotionUtils() {
    }

    /**
     * Returns the last activity recorded for the specified train as a motion.
     *
     * @param world world containing the train
     * @param player train owner
     * @param trainId train identifier
     * @return the train's final recorded motion
     */
    public static TrainMotion lastMotion(UnmodifiableWorld world, Player player, int trainId) {
        BidirectionalIterator<Activity> bidirectionalIterator = world.getTrain(player, trainId).getActivities();
        bidirectionalIterator.gotoLast();
        return (TrainMotion) bidirectionalIterator.get();
    }
}
