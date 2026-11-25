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

import freerails.model.operations.station.Station;
import freerails.model.operations.train.schedule.ScheduleState;
import freerails.model.operations.train.schedule.UnmodifiableSchedule;
import freerails.model.player.Player;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.world.World;
import freerails.util.Utils;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.util.Objects;

/**
 * Replaces the schedule of one of the player's trains. This is the only train
 * change a client may send; every stop must be one of the player's stations.
 *
 * @param player the train owner
 * @param trainId the train
 * @param schedule the new schedule
 */
public record ChangeTrainScheduleMove(Player player, int trainId, UnmodifiableSchedule schedule) implements ClientMove {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates the move; the schedule is copied.
     *
     * @param player the train owner
     * @param trainId the train
     * @param schedule the new schedule
     */
    public ChangeTrainScheduleMove {
        Objects.requireNonNull(player);
        schedule = new ScheduleState(Objects.requireNonNull(schedule));
    }

    /**
     * @param player the player the move acts for
     * @return this move changing a train of the supplied player
     */
    @Override
    public @NotNull ChangeTrainScheduleMove withPlayer(@NotNull Player player) {
        return new ChangeTrainScheduleMove(player, trainId, schedule);
    }

    /**
     * Checks that the train is the player's and every stop is one of the player's stations.
     *
     * @param world the world whose state is used
     * @return a status indicating whether the move can be applied
     */
    @Override
    public @NotNull Status applicable(@NotNull UnmodifiableWorld world) {
        if (!Utils.containsId(trainId, world.getTrains(player))) {
            return Status.fail("Unknown train");
        }
        for (int i = 0; i < schedule.getNumberOfOrders(); i++) {
            int stationId = schedule.getOrder(i).getStationId();
            if (world.getStations(player).stream().map(Station::getId).noneMatch(id -> id == stationId)) {
                return Status.fail("A train may only stop at its owner's stations.");
            }
        }
        return Status.OK;
    }

    /**
     * Replaces the train's schedule.
     *
     * @param world the world whose state is updated
     */
    @Override
    public void apply(@NotNull World world) {
        Status status = applicable(world);
        if (!status.isSuccess()) {
            throw new IllegalStateException(status.getMessage());
        }
        new UpdateTrainMove(player, trainId, null, null, schedule).apply(world);
    }
}
