/*
 * Created on 02-Jul-2005
 *
 */
package jfreerails.move;

import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.AKEY;
import jfreerails.world.top.Activity;
import jfreerails.world.top.ActivityIterator;
import jfreerails.world.top.World;

public class NextActivityMove implements Move {

	private static final long serialVersionUID = -1783556069173689661L;
	
	private final Activity activity;
	
	private final FreerailsPrincipal principal;
	
	private final AKEY listKey;
    private final int index;

	public NextActivityMove(Activity activity, int index, AKEY key, FreerailsPrincipal principal) {
		this.activity = activity;
		this.index = index;
		listKey = key;
		this.principal = principal;
	}

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NextActivityMove)) return false;

        final NextActivityMove nextActivityMove = (NextActivityMove) o;

        if (index != nextActivityMove.index) return false;
        if (!activity.equals(nextActivityMove.activity)) return false;
        if (!listKey.equals(nextActivityMove.listKey)) return false;
        if (!principal.equals(nextActivityMove.principal)) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = activity.hashCode();
        result = 29 * result + principal.hashCode();
        result = 29 * result + listKey.hashCode();
        result = 29 * result + index;
        return result;
    }

	public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
		//Check that active entity exists.
		if(w.size(listKey, principal) <= index)
			return MoveStatus.moveFailed("Index out of range.");
		
		return MoveStatus.MOVE_OK;
	}

	public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
		ActivityIterator ai = w.getActivities(listKey, index, principal);
		while(ai.hasNext())
			ai.nextActivity();
		
		Activity act = ai.getActivity();
		if(act.equals(activity))
			return MoveStatus.MOVE_OK;
		
		return MoveStatus.moveFailed("Expected "+activity+ " but found "+act);
	}

	public MoveStatus doMove(World w, FreerailsPrincipal p) {
		MoveStatus ms = tryDoMove(w, p);
		if(ms.ok)
			w.add(listKey, index, activity, principal);
		return ms;
	}

	public MoveStatus undoMove(World w, FreerailsPrincipal p) {
		MoveStatus ms = tryUndoMove(w, p);
		if(ms.ok)
			w.removeLastActivity(listKey, index, principal);
		return ms;
	}

}
