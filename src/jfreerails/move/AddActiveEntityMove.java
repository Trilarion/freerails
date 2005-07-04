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

public class AddActiveEntityMove implements Move {

	private static final long serialVersionUID = 8732702087937675013L;

	private final Activity activity;

	private final FreerailsPrincipal principal;

	private final AKEY listKey;

	private final int index;

	public AddActiveEntityMove(Activity activity, int index, AKEY key,
			FreerailsPrincipal principal) {
		this.activity = activity;
		this.index = index;
		listKey = key;
		this.principal = principal;
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof AddActiveEntityMove))
			return false;

		final AddActiveEntityMove addActiveEntityMove = (AddActiveEntityMove) o;

		if (index != addActiveEntityMove.index)
			return false;
		if (!activity.equals(addActiveEntityMove.activity))
			return false;
		if (!listKey.equals(addActiveEntityMove.listKey))
			return false;
		if (!principal.equals(addActiveEntityMove.principal))
			return false;

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
		if (index != w.size(listKey, principal))
			return MoveStatus.moveFailed("index != w.size(listKey, p)");

		return MoveStatus.MOVE_OK;
	}

	public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
		int expectedSize = index + 1;
		if (expectedSize != w.size(listKey, principal))
			return MoveStatus
					.moveFailed("(index + 1) != w.size(listKey, principal)");

		ActivityIterator ai = w.getActivities(listKey, index, principal);
		if (ai.hasNext())
			return MoveStatus
					.moveFailed("There should be exactly one activity!");

		Activity act = ai.getActivity();

		if (!act.equals(activity))
			return MoveStatus.moveFailed("Expected " + activity.toString()
					+ " but found " + act.toString());

		return MoveStatus.MOVE_OK;
	}

	public MoveStatus doMove(World w, FreerailsPrincipal p) {
		MoveStatus ms = tryDoMove(w, p);
		if (ms.ok)
			w.addActiveEntity(listKey, activity, principal);

		return ms;
	}

	public MoveStatus undoMove(World w, FreerailsPrincipal p) {
		MoveStatus ms = tryUndoMove(w, p);
		if (ms.ok)
			w.removeLastActiveEntity(listKey, principal);

		return ms;
	}

}
