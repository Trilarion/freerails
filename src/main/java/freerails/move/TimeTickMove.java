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

import freerails.world.ReadOnlyWorld;
import freerails.world.World;
import freerails.world.game.GameTime;
import freerails.world.player.FreerailsPrincipal;

/**
 * Changes the time item on the world object.
 */
public class TimeTickMove implements Move {

    private static final long serialVersionUID = 3257290240212153393L;
    private final GameTime oldTime;
    private final GameTime newTime;

    /**
     * @param oldTime
     * @param newTime
     */
    public TimeTickMove(GameTime oldTime, GameTime newTime) {
        this.oldTime = oldTime;
        this.newTime = newTime;
    }

    /**
     * @param world
     * @return
     */
    public static Move getMove(ReadOnlyWorld world) {

        GameTime oldTime = world.currentTime();
        GameTime newTime = new GameTime(oldTime.getTicks() + 1);

        return new TimeTickMove(oldTime, newTime);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TimeTickMove)) return false;

        final TimeTickMove timeTickMove = (TimeTickMove) obj;

        if (!newTime.equals(timeTickMove.newTime)) return false;
        return oldTime.equals(timeTickMove.oldTime);
    }

    @Override
    public int hashCode() {
        int result;
        result = oldTime.hashCode();
        result = 29 * result + newTime.hashCode();
        return result;
    }

    public MoveStatus tryDoMove(World world, FreerailsPrincipal principal) {
        if (world.currentTime().equals(oldTime)) {
            return MoveStatus.MOVE_OK;
        }
        String string = "oldTime = " + oldTime.getTicks() + " <=> " + "currentTime " + (world.currentTime()).getTicks();

        return MoveStatus.moveFailed(string);
    }

    public MoveStatus tryUndoMove(World world, FreerailsPrincipal principal) {
        GameTime time = world.currentTime();

        if (time.equals(newTime)) {
            return MoveStatus.MOVE_OK;
        }
        return MoveStatus.moveFailed("Expected " + newTime + ", found " + time);
    }

    public MoveStatus doMove(World world, FreerailsPrincipal principal) {
        MoveStatus status = tryDoMove(world, principal);

        if (status.status) {
            world.setTime(newTime);
        }

        return status;
    }

    public MoveStatus undoMove(World world, FreerailsPrincipal principal) {
        MoveStatus status = tryUndoMove(world, principal);

        if (status.isStatus()) {
            world.setTime(oldTime);
        }

        return status;
    }

    @Override
    public String toString() {
        return "TimeTickMove: " + oldTime + "=>" + newTime;
    }
}