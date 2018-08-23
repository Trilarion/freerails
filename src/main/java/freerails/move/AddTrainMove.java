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

import freerails.model.player.Player;
import freerails.model.train.Train;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.world.World;
import freerails.util.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Adds a new train to the world.
 */
public class AddTrainMove implements Move {

    private final Player player;
    private final Train train;

    public AddTrainMove(@NotNull Player player, @NotNull Train train) {
        this.player = player;
        this.train = train;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AddTrainMove)) {
            return false;
        }
        final AddTrainMove o = (AddTrainMove) obj;
        return Objects.equals(player, o.player) && Objects.equals(train, o.train);
    }

    @Override
    public int hashCode() {
        return Objects.hash(player, train);
    }

    @NotNull
    @Override
    public Status applicable(@NotNull UnmodifiableWorld world) {
        if (Utils.containsId(train.getId(), world.getTrains(player))) {
            return Status.fail("Train with id already existing.");
        }
        return Status.OK;
    }

    @Override
    public void apply(@NotNull World world) {
        world.addTrain(player, train);
    }
}
