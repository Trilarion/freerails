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

import freerails.model.game.Speed;
import freerails.model.world.World;
import freerails.model.player.Player;
import freerails.nove.Status;

// TODO old speed not really needed here (only for undo)
/**
 * Changes the game speed item on the world object.
 */
public class ChangeGameSpeedMove implements Move {

    private static final long serialVersionUID = 3545794368956086071L;
    private final Speed oldSpeed;
    private final Speed newSpeed;

    public ChangeGameSpeedMove(Speed oldSpeed, Speed newSpeed) {
        this.oldSpeed = oldSpeed;
        this.newSpeed = newSpeed;
    }

    public Status tryDoMove(World world, Player player) {
        Speed actualSpeed = world.getSpeed();

        // check that old speed and actual speed are consistent
        if (actualSpeed.equals(oldSpeed)) {
            return Status.OK;
        }
        String string = "oldSpeed = " + oldSpeed + " <=> " + "currentSpeed " + actualSpeed;
        return Status.fail(string);
    }

    public Status tryUndoMove(World world, Player player) {
        Speed actualSpeed = world.getSpeed();

        if (actualSpeed.equals(newSpeed)) {
            return Status.OK;
        }
        return Status.fail("Expected " + newSpeed + ", found " + actualSpeed);
    }

    public Status doMove(World world, Player player) {
        // TODO is this the convention to try exactly before?
        Status status = tryDoMove(world, player);

        if (status.isSuccess()) {
            world.setSpeed(newSpeed);
        }

        return status;
    }

    public Status undoMove(World world, Player player) {
        // TODO is this the convention to try exactly before
        Status status = tryUndoMove(world, player);

        if (status.isSuccess()) {
            world.setSpeed(oldSpeed);
        }

        return status;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ChangeGameSpeedMove)) return false;

        final ChangeGameSpeedMove changeGameSpeedMove = (ChangeGameSpeedMove) obj;

        if (!newSpeed.equals(changeGameSpeedMove.newSpeed)) return false;
        return oldSpeed.equals(changeGameSpeedMove.oldSpeed);
    }

    @Override
    public int hashCode() {
        int result;
        result = oldSpeed.hashCode();
        result = 29 * result + newSpeed.hashCode();
        return result;
    }

    /**
     * @return
     */
    public Speed getNewSpeed() {
        return newSpeed;
    }

    @Override
    public String toString() {
        return "ChangeGameSpeedMove: " + oldSpeed + "=>" + newSpeed;
    }
}