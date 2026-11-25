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

package freerails.model.operations.train.motion;

import freerails.model.operations.train.TrainState;
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
        BidirectionalIterator<TrainMotion> bidirectionalIterator = world.getTrain(player, trainId).getActivities();
        bidirectionalIterator.gotoLast();
        return bidirectionalIterator.previous();
    }

    /**
     * Returns whether the specified train's latest movement started from a station stop, that is,
     * whether the train left a station with that movement. Stationary activities recorded after the
     * movement, such as a path lengthened for added wagons, are ignored.
     *
     * @param world world containing the train
     * @param player train owner
     * @param trainId train identifier
     * @return {@code true} if the latest movement directly follows a stop at a station
     */
    public static boolean latestMovementLeavesStation(UnmodifiableWorld world, Player player, int trainId) {
        BidirectionalIterator<TrainMotion> activities = world.getTrain(player, trainId).getActivities();
        if (!activities.hasNext()) {
            return false;
        }
        activities.gotoLast();

        // Find the latest movement.
        boolean foundMovement = false;
        while (activities.hasPrevious() && !foundMovement) {
            foundMovement = isMovement(activities.previous());
        }
        if (!foundMovement || !activities.hasPrevious()) {
            return false;
        }

        TrainState stateBefore = activities.previous().getTrainState();
        return stateBefore == TrainState.STOPPED_AT_STATION || stateBefore == TrainState.WAITING_FOR_FULL_LOAD;
    }

    /**
     * Returns whether the motion moves the train, as opposed to keeping it stationary.
     *
     * @param motion the motion to check
     * @return {@code true} if the train travels during the motion
     */
    public static boolean isMovement(TrainMotion motion) {
        return motion.getTrainState() == TrainState.READY && motion.getDuration() > 0;
    }
}
