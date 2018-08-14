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
import freerails.model.train.motion.TrainMotion;
import freerails.model.world.World;
import org.jetbrains.annotations.NotNull;

// TODO undo not implemented
/**
 *
 */
public class MoveTrainActivityMove implements Move {

    private final Player player;
    private final int trainId;
    private final TrainMotion trainMotion;


    public MoveTrainActivityMove(@NotNull Player player, int trainId, @NotNull  TrainMotion trainMotion) {
        this.player = player;
        this.trainId = trainId;
        this.trainMotion = trainMotion;
    }

    @Override
    public Status tryDoMove(World world, Player player) {
        return Status.OK;
    }

    @Override
    public Status tryUndoMove(World world, Player player) {
        return Status.OK;
    }

    @Override
    public Status doMove(World world, Player player) {
        world.addActivity(this.player, trainId, trainMotion);
        return Status.OK;
    }

    @Override
    public Status undoMove(World world, Player player) {
        return Status.OK;
    }
}
