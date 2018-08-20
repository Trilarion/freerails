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

import freerails.model.game.Time;
import freerails.model.world.World;
import freerails.model.player.Player;
import freerails.nove.Status;

/**
 * Changes the time item on the world object.
 */
public class TimeTickMove implements Move {

    private static final long serialVersionUID = 3257290240212153393L;
    private final Time oldTime;

    /**
     * @param oldTime
     */
    public TimeTickMove(Time oldTime) {
        this.oldTime = oldTime;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TimeTickMove)) return false;

        final TimeTickMove timeTickMove = (TimeTickMove) obj;
        return oldTime.equals(timeTickMove.oldTime);
    }

    @Override
    public int hashCode() {
        int result;
        result = oldTime.hashCode();
        return result;
    }

    public Status tryDoMove(World world, Player player) {
        if (world.getClock().getCurrentTime().equals(oldTime)) {
            return Status.OK;
        }
        String string = "oldTime = " + oldTime.getTicks() + " <=> " + "currentTime " + world.getClock().getCurrentTime().getTicks();

        return Status.fail(string);
    }

    public Status tryUndoMove(World world, Player player) {
        Time time = world.getClock().getCurrentTime();
        // TODO doesn't work anymore, we also advance the time
        return Status.OK;
    }

    public Status doMove(World world, Player player) {
        Status status = tryDoMove(world, player);

        if (status.isSuccess()) {
            world.getClock().advanceTime();
        }

        return status;
    }

    public Status undoMove(World world, Player player) {
        Status status = tryUndoMove(world, player);
        // TODO doesn't work anymore, we also advance the time

        return status;
    }

    @Override
    public String toString() {
        return "TimeTickMove: " + oldTime + " advance";
    }
}