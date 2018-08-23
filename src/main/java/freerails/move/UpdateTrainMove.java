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

package freerails.move;

import freerails.model.cargo.UnmodifiableCargoBatchBundle;
import freerails.model.player.Player;
import freerails.model.train.Train;
import freerails.model.train.schedule.UnmodifiableSchedule;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.world.World;
import freerails.util.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 *
 */
public class UpdateTrainMove implements Move {

    private final Player player;
    private final int trainId;
    private final UnmodifiableCargoBatchBundle cargoBatchBundle;
    private final List<Integer> consist;
    private final UnmodifiableSchedule schedule;

    public UpdateTrainMove(@NotNull Player player, int trainId, UnmodifiableCargoBatchBundle cargoBatchBundle, List<Integer> consist, UnmodifiableSchedule schedule) {
        if (cargoBatchBundle == null && consist == null && schedule == null) {
            throw new IllegalArgumentException("Not all parameters can be null.");
        }
        this.player = player;
        this.trainId = trainId;
        this.cargoBatchBundle = cargoBatchBundle;
        this.consist = consist;
        this.schedule = schedule;
    }

    @Override
    public @NotNull Status applicable(@NotNull UnmodifiableWorld world) {
        if (!Utils.containsId(trainId, world.getTrains(player))) {
            Status.fail("Unknown train");
        }
        return Status.OK;
    }

    @Override
    public void apply(@NotNull World world) {
        Train train = world.getTrain(player, trainId);
        if (cargoBatchBundle != null) {
            train.setCargoBatchBundle(cargoBatchBundle);
        }
        if (consist != null) {
            train.setConsist(consist);
        }
        if (schedule != null) {
            train.setSchedule(schedule);
        }
    }
}
