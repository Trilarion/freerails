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

/*
 *
 */
package freerails.move.listmove;

import freerails.move.MoveStatus;
import freerails.util.Utils;
import freerails.model.world.PlayerKey;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.world.World;
import freerails.model.player.Player;

import java.io.Serializable;

/**
 * All Moves that replace an item in a list with another should extend this
 * class.
 */
public class ChangeItemInListMove implements ListMove {

    private static final long serialVersionUID = -4457694821370844051L;
    private final PlayerKey listPlayerKey;
    private final int index;
    private final Serializable before;
    private final Serializable after;
    private final Player player;

    /**
     * @param k
     * @param index
     * @param before
     * @param after
     * @param player
     */
    public ChangeItemInListMove(PlayerKey k, int index, Serializable before, Serializable after, Player player) {
        this.before = before;
        this.after = after;
        this.index = index;
        listPlayerKey = k;
        this.player = player;
    }

    /**
     * @return
     */
    public boolean beforeEqualsAfter() {
        return Utils.equal(before, after);
    }

    public MoveStatus doMove(World world, Player player) {
        return move(after, before, world);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ChangeItemInListMove) {
            ChangeItemInListMove test = (ChangeItemInListMove) obj;

            if (!before.equals(test.before)) {
                return false;
            }

            if (!after.equals(test.after)) {
                return false;
            }

            if (index != test.index) {
                return false;
            }

            return listPlayerKey == test.listPlayerKey;
        }
        return false;
    }

    public int getIndex() {
        return index;
    }

    public PlayerKey getKey() {
        return listPlayerKey;
    }

    /**
     * @return
     */
    public Player getPlayer() {
        return player;
    }

    @Override
    public int hashCode() {
        int result;
        result = listPlayerKey.hashCode();
        result = 29 * result + index;
        result = 29 * result + (before != null ? before.hashCode() : 0);
        result = 29 * result + (after != null ? after.hashCode() : 0);
        result = 29 * result + player.hashCode();

        return result;
    }

    /**
     * @param to
     * @param from
     * @param world
     * @return
     */
    private MoveStatus move(Serializable to, Serializable from, World world) {
        MoveStatus moveStatus = tryMove(to, from, world);

        if (moveStatus.succeeds()) {
            world.set(player, listPlayerKey, index, to);
        }

        return moveStatus;
    }

    @Override
    public String toString() {
        return getClass().getName() + " before: " + before.toString() + " after: " + after.toString();
    }

    public MoveStatus tryDoMove(World world, Player player) {
        return tryMove(after, before, world);
    }

    /**
     * @param to
     * @param from
     * @param world
     * @return
     */
    private MoveStatus tryMove(Serializable to, Serializable from, UnmodifiableWorld world) {
        if (index >= world.size(player, listPlayerKey)) {
            return MoveStatus.moveFailed("world.size(listKey) is " + world.size(player, listPlayerKey) + " but index is " + index);
        }

        Serializable item2change = world.get(player, listPlayerKey, index);

        if (null == item2change) {
            if (null == from) {
                return MoveStatus.MOVE_OK;
            }
            return MoveStatus.moveFailed("Expected null but found " + from);
        }
        if (!from.equals(item2change)) {
            String message = "Expected " + from.toString() + " but found " + to.toString();
            return MoveStatus.moveFailed(message);
        }
        return MoveStatus.MOVE_OK;
    }

    public MoveStatus tryUndoMove(World world, Player player) {
        return tryMove(before, after, world);
    }

    public MoveStatus undoMove(World world, Player player) {
        return move(before, after, world);
    }
}