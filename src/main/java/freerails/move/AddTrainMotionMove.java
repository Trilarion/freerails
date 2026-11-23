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

package freerails.move;

import freerails.model.player.Player;
import freerails.model.operations.train.motion.TrainMotion;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.world.World;
import freerails.util.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Appends the next motion to a train's timeline of motions; this is the move
 * that moves trains.
 *
 * <p>A {@link TrainMotion} (also called an activity) holds the path, train
 * length, speed profile, duration, and state of one segment of the train's
 * movement. A train's position at any time is derived from its motions, so
 * moving a train means appending its next motion. Effects of a station stop,
 * such as cargo transfer and consist changes, are separate moves that are
 * combined with this one in a {@link CompositeMove}.
 */
public class AddTrainMotionMove implements Move {

    private final Player player;
    private final int trainId;
    private final TrainMotion activity;


    /**
     * Creates a move that appends the supplied motion to a train.
     *
     * @param player owner of the train
     * @param trainId identifier of the train
     * @param activity motion to append
     */
    public AddTrainMotionMove(@NotNull Player player, int trainId, @NotNull TrainMotion activity) {
        this.player = player;
        this.trainId = trainId;
        this.activity = activity;
    }

    /**
     * Checks whether this move can be applied to the supplied world, which
     * requires the train to exist for the player.
     *
     * @param world the world whose state is used or updated
     * @return a status indicating whether the move can be applied
     */
    @NotNull
    @Override
    public Status applicable(@NotNull UnmodifiableWorld world) {
        if (!Utils.containsId(trainId, world.getTrains(player))) {
            return Status.fail("Unknown train");
        }
        return Status.OK;
    }

    /**
     * Applies this move to the supplied world.
     *
     * @param world the world whose state is used or updated
     */
    @Override
    public void apply(@NotNull World world) {
        world.addActivity(player, trainId, activity);
    }

    /**
     * Returns whether this object is equal to the specified object.
     *
     * @param obj the object to compare with this object
     * @return true if this object is equal to the specified object, otherwise false
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AddTrainMotionMove o)) {
            return false;
        }
        return Objects.equals(player, o.player) && trainId == o.trainId && Objects.equals(activity, o.activity);
    }

    /**
     * Returns a hash code for this object.
     *
     * @return the hash code for this object
     */
    @Override
    public int hashCode() {
        return Objects.hash(player, trainId, activity);
    }
}
