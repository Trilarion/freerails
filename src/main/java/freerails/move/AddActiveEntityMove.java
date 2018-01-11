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

import freerails.world.Activity;
import freerails.world.ActivityIterator;
import freerails.world.World;
import freerails.world.player.FreerailsPrincipal;

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
    private final FreerailsPrincipal principal;
    private final int index;

    /**
     * @param activity
     * @param index
     * @param principal
     */
    public AddActiveEntityMove(Activity activity, int index,
                               FreerailsPrincipal principal) {
        this.activity = activity;
        this.index = index;

        this.principal = principal;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof AddActiveEntityMove))
            return false;

        final AddActiveEntityMove addActiveEntityMove = (AddActiveEntityMove) obj;

        if (index != addActiveEntityMove.index)
            return false;
        if (!activity.equals(addActiveEntityMove.activity))
            return false;

        return principal.equals(addActiveEntityMove.principal);
    }

    @Override
    public int hashCode() {
        int result;
        result = activity.hashCode();
        result = 29 * result + principal.hashCode();

        result = 29 * result + index;
        return result;
    }

    public MoveStatus tryDoMove(World world, FreerailsPrincipal principal) {
        if (index != world.size(this.principal))
            return MoveStatus.moveFailed("index != w.size(listKey, p)");

        return MoveStatus.MOVE_OK;
    }

    public MoveStatus tryUndoMove(World world, FreerailsPrincipal principal) {
        int expectedSize = index + 1;
        if (expectedSize != world.size(this.principal))
            return MoveStatus
                    .moveFailed("(index + 1) != w.size(listKey, principal)");

        ActivityIterator ai = world.getActivities(this.principal, index);
        if (ai.hasNext())
            return MoveStatus
                    .moveFailed("There should be exactly one activity!");

        Activity act = ai.getActivity();

        if (!act.equals(activity))
            return MoveStatus.moveFailed("Expected " + activity.toString()
                    + " but found " + act.toString());

        return MoveStatus.MOVE_OK;
    }

    public MoveStatus doMove(World world, FreerailsPrincipal principal) {
        MoveStatus ms = tryDoMove(world, principal);
        if (ms.ok)
            world.addActiveEntity(this.principal, activity);

        return ms;
    }

    public MoveStatus undoMove(World world, FreerailsPrincipal principal) {
        MoveStatus ms = tryUndoMove(world, principal);
        if (ms.ok)
            world.removeLastActiveEntity(this.principal);

        return ms;
    }

}
