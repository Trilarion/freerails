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

import freerails.model.train.activity.Activity;
import freerails.util.BidirectionalIterator;
import freerails.model.world.World;
import freerails.model.player.Player;
import freerails.nove.Status;

/**
 *
 */
public class NextActivityMove implements Move {

    private static final long serialVersionUID = -1783556069173689661L;
    private final Activity activity;
    private final Player player;
    private final int trainId;

    /**
     * @param activity
     * @param trainId
     * @param player
     */
    public NextActivityMove(Activity activity, int trainId, Player player) {
        this.activity = activity;
        this.trainId = trainId;

        this.player = player;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NextActivityMove)) return false;

        final NextActivityMove nextActivityMove = (NextActivityMove) obj;

        if (trainId != nextActivityMove.trainId) return false;
        if (!activity.equals(nextActivityMove.activity)) return false;
        return player.equals(nextActivityMove.player);
    }

    @Override
    public int hashCode() {
        int result;
        result = activity.hashCode();
        result = 29 * result + player.hashCode();

        result = 29 * result + trainId;
        return result;
    }

    public Status tryDoMove(World world, Player player) {
        // Check that active entity exists.
        // TODO we change activities anyway, therefore that gets commented out
        // if (world.size(this.player) <= index)
        //    return MoveStatus.moveFailed("Index out of range. " + world.size(this.player) + "<= " + index);

        return Status.OK;
    }

    public Status tryUndoMove(World world, Player player) {
        BidirectionalIterator<Activity> activityIterator = world.getTrain(this.player, trainId).getActivities();
        activityIterator.gotoLast();

        Activity activity = activityIterator.get();
        if (activity.equals(this.activity)) return Status.OK;

        return Status.fail("Expected " + this.activity + " but found " + activity);
    }

    public Status doMove(World world, Player player) {
        Status status = tryDoMove(world, player);
        if (status.isSuccess()) world.addActivity(this.player, trainId, activity);
        return status;
    }

    public Status undoMove(World world, Player player) {
        Status status = tryUndoMove(world, player);
        if (status.isSuccess()) world.getTrain(this.player, trainId).removeLastActivity();
        return status;
    }
}
