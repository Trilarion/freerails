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

import freerails.world.ITEM;
import freerails.world.world.ReadOnlyWorld;
import freerails.world.world.World;
import freerails.world.game.GameSpeed;
import freerails.world.player.FreerailsPrincipal;

/**
 * Changes the game speed item on the world object.
 */
public class ChangeGameSpeedMove implements Move {

    private static final long serialVersionUID = 3545794368956086071L;
    private final GameSpeed oldSpeed;
    private final GameSpeed newSpeed;

    private ChangeGameSpeedMove(GameSpeed before, GameSpeed after) {
        oldSpeed = before;
        newSpeed = after;
    }

    /**
     * @param w
     * @param newGameSpeed
     * @return
     */
    public static Move getMove(ReadOnlyWorld w, GameSpeed newGameSpeed) {
        return new ChangeGameSpeedMove((GameSpeed) w.get(ITEM.GAME_SPEED), newGameSpeed);
    }

    public MoveStatus tryDoMove(World world, FreerailsPrincipal principal) {
        if (world.get(ITEM.GAME_SPEED).equals(oldSpeed)) {
            return MoveStatus.MOVE_OK;
        }
        String string = "oldSpeed = " + oldSpeed.getSpeed() + " <=> " + "currentSpeed " + ((GameSpeed) world.get(ITEM.GAME_SPEED)).getSpeed();

        return MoveStatus.moveFailed(string);
    }

    public MoveStatus tryUndoMove(World world, FreerailsPrincipal principal) {
        GameSpeed speed = ((GameSpeed) world.get(ITEM.GAME_SPEED));

        if (speed.equals(newSpeed)) {
            return MoveStatus.MOVE_OK;
        }
        return MoveStatus.moveFailed("Expected " + newSpeed + ", found " + speed);
    }

    public MoveStatus doMove(World world, FreerailsPrincipal principal) {
        MoveStatus status = tryDoMove(world, principal);

        if (status.succeeds()) {
            world.set(ITEM.GAME_SPEED, newSpeed);
        }

        return status;
    }

    public MoveStatus undoMove(World world, FreerailsPrincipal principal) {
        MoveStatus status = tryUndoMove(world, principal);

        if (status.succeeds()) {
            world.set(ITEM.GAME_SPEED, oldSpeed);
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
    public int getNewSpeed() {
        return newSpeed.getSpeed();
    }

    @Override
    public String toString() {
        return "ChangeGameSpeedMove: " + oldSpeed + "=>" + newSpeed;
    }
}