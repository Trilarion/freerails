/*
 * Created on 13-Apr-2003
 *
 */
package jfreerails.move;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.player.Player;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.KEY;
import jfreerails.world.top.World;

/**
 * All moves that remove an item from a list should extend this class.
 * @author Luke
 *
 */
public class RemoveItemFromListMove implements ListMove {
    private final FreerailsSerializable item;
    private final KEY listKey;
    private final int index;

    private FreerailsPrincipal principal;

    public int getIndex() {
        return index;
    }

    public KEY getKey() {
        return listKey;
    }

    public FreerailsPrincipal getPrincipal() {
	return principal;
    }

    protected RemoveItemFromListMove(KEY k, int i, FreerailsSerializable item,
	    FreerailsPrincipal p) {
        this.item = item;
        this.listKey = k;
        this.index = i;
	this.principal = p;
    }

    protected RemoveItemFromListMove(KEY k, int i, FreerailsSerializable item) {
	this(k, i, item, Player.NOBODY);
    }

    public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
        if (w.size(listKey, p) < (index + 1)) {
            return MoveStatus.moveFailed("w.size(listKey)=" + w.size(listKey, p) +
                " but index =" + index);
        }

        FreerailsSerializable item2remove = w.get(listKey, index, p);

        if (null == item2remove) {
            return MoveStatus.moveFailed("The item at position " + index +
                " has already been removed.");
        }

        if (!item.equals(item2remove)) {
            String reason = "The item at position " + index + " in the list (" +
                item2remove.toString() + ") is not the expected item (" +
                item.toString() + ").";

            return MoveStatus.moveFailed(reason);
        } else {
            return MoveStatus.MOVE_OK;
        }
    }

    public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
        if (w.size(listKey, p) < (index + 1)) {
	    return MoveStatus.moveFailed("w.size(listKey)=" + w.size(listKey,
			p) + " but index =" + index);
        }

        if (null != w.get(listKey, index, p)) {
            String reason = "The item at position " + index + " in the list (" +
                w.get(listKey, index, p).toString() +
                ") is not the expected item (null).";

            return MoveStatus.moveFailed(reason);
        } else {
            return MoveStatus.MOVE_OK;
        }
    }

    public MoveStatus doMove(World w, FreerailsPrincipal p) {
        MoveStatus ms = tryDoMove(w, p);

        if (ms.isOk()) {
            w.set(listKey, index, null, p);
        }

        return ms;
    }

    public MoveStatus undoMove(World w, FreerailsPrincipal p) {
        MoveStatus ms = tryUndoMove(w, p);

        if (ms.isOk()) {
            w.set(listKey, index, this.item, p);
        }

        return ms;
    }

    public boolean equals(Object o) {
        if (o instanceof RemoveItemFromListMove) {
            RemoveItemFromListMove test = (RemoveItemFromListMove)o;

	    if (! principal.equals(test.principal)) {
		return false;
	    }

            if (!this.item.equals(test.getBefore())) {
                return false;
            }

            if (this.index != test.index) {
                return false;
            }

            if (this.listKey != test.listKey) {
                return false;
            }

            return true;
        } else {
            return false;
        }
    }

    public FreerailsSerializable getBefore() {
        return item;
    }

    public FreerailsSerializable getAfter() {
        return null;
    }
}
