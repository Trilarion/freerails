/*
 * Created on 02-Jul-2005
 *
 */
package freerails.move;

import freerails.world.common.Activity;
import freerails.world.common.ActivityIterator;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.top.World;

public class NextActivityMove implements Move {

    private static final long serialVersionUID = -1783556069173689661L;

    private final Activity activity;

    private final FreerailsPrincipal principal;

    private final int index;

    public NextActivityMove(Activity activity, int index,
                            FreerailsPrincipal principal) {
        this.activity = activity;
        this.index = index;

        this.principal = principal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof NextActivityMove))
            return false;

        final NextActivityMove nextActivityMove = (NextActivityMove) o;

        if (index != nextActivityMove.index)
            return false;
        if (!activity.equals(nextActivityMove.activity))
            return false;
        return principal.equals(nextActivityMove.principal);
    }

    @Override
    public int hashCode() {
        int result;
        result = activity.hashCode();
        result = 29 * result + principal.hashCode();

        result = 29 * result + index;
        return result;
    }

    public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
        // Check that active entity exists.
        if (w.size(principal) <= index)
            return MoveStatus.moveFailed("Index out of range. "
                    + w.size(principal) + "<= " + index);

        return MoveStatus.MOVE_OK;
    }

    public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
        ActivityIterator ai = w.getActivities(principal, index);
        ai.gotoLastActivity();

        Activity act = ai.getActivity();
        if (act.equals(activity))
            return MoveStatus.MOVE_OK;

        return MoveStatus.moveFailed("Expected " + activity + " but found "
                + act);
    }

    public MoveStatus doMove(World w, FreerailsPrincipal p) {
        MoveStatus ms = tryDoMove(w, p);
        if (ms.ok)
            w.add(principal, index, activity);
        return ms;
    }

    public MoveStatus undoMove(World w, FreerailsPrincipal p) {
        MoveStatus ms = tryUndoMove(w, p);
        if (ms.ok)
            w.removeLastActivity(principal, index);
        return ms;
    }

}
