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
package freerails.move;

import freerails.model.activity.Activity;
import freerails.model.activity.ActivityIterator;
import freerails.model.world.World;
import freerails.model.player.Player;

/**
 * A move that adds an active entity. An active entity is something whose state
 * may be continually changing. An example is a train - it is an active entity
 * since while it is moving its position is continually changing.
 *
 * @see NextActivityMove
 */
public class AddActiveEntityMove implements Move {

    private static final long serialVersionUID = 8732702087937675013L;
    private final Activity activity;
    private final Player player;
    private final int index;

    /**
     * @param activity
     * @param index
     * @param player
     */
    public AddActiveEntityMove(Activity activity, int index, Player player) {
        this.activity = activity;
        this.index = index;

        this.player = player;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof AddActiveEntityMove)) return false;

        final AddActiveEntityMove addActiveEntityMove = (AddActiveEntityMove) obj;

        if (index != addActiveEntityMove.index) return false;
        if (!activity.equals(addActiveEntityMove.activity)) return false;

        return player.equals(addActiveEntityMove.player);
    }

    @Override
    public int hashCode() {
        int result;
        result = activity.hashCode();
        result = 29 * result + player.hashCode();

        result = 29 * result + index;
        return result;
    }

    public Status tryDoMove(World world, Player player) {
        if (index != world.size(this.player)) return Status.moveFailed("index != world.size(listKey, p)");

        return Status.OK;
    }

    public Status tryUndoMove(World world, Player player) {
        int expectedSize = index + 1;
        if (expectedSize != world.size(this.player))
            return Status.moveFailed("(index + 1) != world.size(listKey, player)");

        ActivityIterator ai = world.getActivities(this.player, index);
        if (ai.hasNext()) return Status.moveFailed("There should be exactly one activity!");

        Activity act = ai.getActivity();

        if (!act.equals(activity))
            return Status.moveFailed("Expected " + activity.toString() + " but found " + act.toString());

        return Status.OK;
    }

    public Status doMove(World world, Player player) {
        Status status = tryDoMove(world, player);
        if (status.succeeds()) world.addActiveEntity(this.player, activity);

        return status;
    }

    public Status undoMove(World world, Player player) {
        Status status = tryUndoMove(world, player);
        if (status.succeeds()) world.removeLastActiveEntity(this.player);

        return status;
    }

}
