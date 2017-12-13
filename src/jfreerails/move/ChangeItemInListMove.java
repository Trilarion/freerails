/*
 * Created on 13-Apr-2003
 *
 */
package jfreerails.move;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.top.KEY;
import jfreerails.world.top.World;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.player.Player;

/**
 * All Moves that replace an item in a list with another should extend this class.
 *
 * @author Luke
 *
 */
public abstract class ChangeItemInListMove implements ListMove {
    final KEY listKey;
    final int index;
    private final FreerailsSerializable before;
    private final FreerailsSerializable after;
    private final FreerailsPrincipal principal;

    public FreerailsPrincipal getPrincipal() {
	return principal;
    }

    public int getIndex() {
        return index;
    }

    public KEY getKey() {
        return listKey;
    }

    protected ChangeItemInListMove(KEY k, int index, FreerailsSerializable before, FreerailsSerializable after, FreerailsPrincipal principal) {
        this.before = before;
        this.after = after;
        this.index = index;
        this.listKey = k;
	this.principal = principal;
    }

    /**
     * @deprecated in favour of ChangItemInListMove(KEY, int,
     * FreerailsSerializable, FreerailsSerializable, FreerailsPrincipal)
     */
    protected ChangeItemInListMove(KEY k, int index,
        FreerailsSerializable before, FreerailsSerializable after) {
	this(k, index, before, after, Player.NOBODY);
    }

    public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
        return tryMove(this.after, this.before, w, p);
    }

    public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
        return tryMove(this.before, this.after, w, p);
    }

    public MoveStatus doMove(World w, FreerailsPrincipal p) {
        return move(this.after, this.before, w, p);
    }

    public MoveStatus undoMove(World w, FreerailsPrincipal p) {
        return move(this.before, this.after, w, p);
    }

    protected MoveStatus tryMove(FreerailsSerializable to,
        FreerailsSerializable from, World w, FreerailsPrincipal p) {
        if (index >= w.size(this.listKey, p)) {
            return MoveStatus.moveFailed("w.size(this.listKey, p) is " +
                w.size(this.listKey, p) + " but index is " + index);
        }

        FreerailsSerializable item2change = w.get(listKey, index, p);

        if (null == item2change) {
            if (null == from) {
                return MoveStatus.MOVE_OK;
            } else {
                return MoveStatus.moveFailed("Attempt to change null object");
            }
        } else {
            if (!from.equals(item2change)) {
                return MoveStatus.moveFailed("Expected " + from.toString() +
                    " but found " + to.toString());
            } else {
                return MoveStatus.MOVE_OK;
            }
        }
    }

    protected MoveStatus move(FreerailsSerializable to,
        FreerailsSerializable from, World w, FreerailsPrincipal p) {
        MoveStatus ms = tryMove(to, from, w, p);

        if (ms.ok) {
	    w.set(this.listKey, index, to, p);
        }

        return ms;
    }

    public boolean equals(Object o) {
        if (o instanceof ChangeItemInListMove) {
            ChangeItemInListMove test = (ChangeItemInListMove)o;

	    if (!principal.equals(test.principal)) {
		return false;
	    }

            if (!this.before.equals(test.getBefore())) {
                return false;
            }

            if (!this.after.equals(test.getAfter())) {
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

    public FreerailsSerializable getAfter() {
        return after;
    }

    public FreerailsSerializable getBefore() {
        return before;
    }
}
