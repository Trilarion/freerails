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

import freerails.model.operations.cargo.ImmutableCargoBatchBundle;
import freerails.model.operations.cargo.UnmodifiableCargoBatchBundle;
import freerails.model.player.Player;
import freerails.model.operations.train.Train;
import freerails.model.operations.train.TrainConfiguration;
import freerails.model.operations.train.TrainRuntimeState;
import freerails.model.operations.train.schedule.ScheduleState;
import freerails.model.operations.train.schedule.UnmodifiableSchedule;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.world.World;
import freerails.util.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * Replaces a player's train with its updated state.
 */
public class UpdateTrainMove implements Move {

    private final Player player;
    private final int trainId;
    private final UnmodifiableCargoBatchBundle cargoBatchBundle;
    private final List<Integer> consist;
    private final UnmodifiableSchedule schedule;

    /**
     * Creates an update train move from the supplied values.
     *
     * @param player the player
     * @param trainId the identifier to use
     * @param cargoBatchBundle the cargo batch bundle
     * @param consist the consist
     * @param schedule the schedule
     */
    public UpdateTrainMove(@NotNull Player player, int trainId, UnmodifiableCargoBatchBundle cargoBatchBundle, List<Integer> consist, UnmodifiableSchedule schedule) {
        if (cargoBatchBundle == null && consist == null && schedule == null) {
            throw new IllegalArgumentException("Not all parameters can be null.");
        }
        this.player = player;
        this.trainId = trainId;
        this.cargoBatchBundle = cargoBatchBundle == null ? null : new ImmutableCargoBatchBundle(cargoBatchBundle);
        this.consist = consist == null ? null : List.copyOf(consist);
        this.schedule = schedule == null ? null : new ScheduleState(schedule);
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
        if (!(obj instanceof UpdateTrainMove other)) {
            return false;
        }
        return trainId == other.trainId
                && Objects.equals(player, other.player)
                && Objects.equals(cargoBatchBundle, other.cargoBatchBundle)
                && Objects.equals(consist, other.consist)
                && Objects.equals(schedule, other.schedule);
    }

    /**
     * Returns a hash code for this object.
     *
     * @return the hash code for this object
     */
    @Override
    public int hashCode() {
        return Objects.hash(player, trainId, cargoBatchBundle, consist, schedule);
    }

    /**
     * Checks whether this move can be applied to the supplied world.
     *
     * @param world the world whose state is used or updated
     * @return a status indicating whether the move can be applied
     */
    @Override
    public @NotNull Status applicable(@NotNull UnmodifiableWorld world) {
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
        Train train = world.getTrain(player, trainId);
        TrainRuntimeState runtimeState = train.getRuntimeState();
        if (cargoBatchBundle != null) {
            runtimeState.setCargoBatchBundle(cargoBatchBundle);
        }
        if (consist != null) {
            train.setConfiguration(new TrainConfiguration(train.getEngine(), consist));
        }
        if (schedule != null) {
            runtimeState.setSchedule(schedule);
        }
    }
}
