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
 * All moves that add an item to a list should extend this class.
 *
 * @author Luke
 *
 */
public class AddItemToListMove implements ListMove {
    final KEY listKey;
    final int index;
    protected final FreerailsSerializable item;
    protected final FreerailsPrincipal principal;

    public FreerailsPrincipal getPrincipal() {
	return principal;
    }

    public int getIndex() {
        return index;
    }

    public KEY getKey() {
        return listKey;
    }

    /**
     * @deprecated in favour of AddItemToListMove(KEY, int,
     * FreerailsSerializable, FreerailsPrincipal)
     */
    protected AddItemToListMove(KEY key, int i, FreerailsSerializable item) {
	this(key, i, item, Player.NOBODY);
    }

    protected AddItemToListMove(KEY key, int i, FreerailsSerializable item,
	    FreerailsPrincipal principal) {
        this.listKey = key;
        this.index = i;
        this.item = item;
	this.principal = principal;
    }

    public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
        if (w.size(listKey, p) != index) {
            return MoveStatus.moveFailed("Expected size of list is " + index +
                " but actual size is " + w.size(listKey, p));
	}

        return MoveStatus.MOVE_OK;
    }
	
    public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
        int expectListSize = index + 1;

        if (w.size(listKey, p) != expectListSize) {
            return MoveStatus.moveFailed("Expected size of list is " +
		    expectListSize + " but actual size is " + w.size(listKey,
			p));
        }

        return MoveStatus.MOVE_OK;
    }

    public MoveStatus doMove(World w, FreerailsPrincipal p) {
        MoveStatus ms = tryDoMove(w, p);

        if (ms.isOk()) {
	    w.add(listKey, this.item, p);
        }

        return ms;
    }

    public MoveStatus undoMove(World w, FreerailsPrincipal p) {
        MoveStatus ms = tryUndoMove(w, p);

        if (ms.isOk()) {
	    w.removeLast(listKey, p);
        }

        return ms;
    }

    public boolean equals(Object o) {
        if (o instanceof AddItemToListMove) {
            AddItemToListMove test = (AddItemToListMove)o;

            if (!this.item.equals(test.getAfter())) {
                return false;
            }

            if (this.index != test.index) {
                return false;
            }

            if (this.listKey != test.listKey) {
                return false;
            }

	    if (! principal.equals(test.principal)) {
		return false;
	    }

            return true;
        } else {
            return false;
        }
    }

    public FreerailsSerializable getBefore() {
        return null;
    }

    public FreerailsSerializable getAfter() {
        return item;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(this.getClass().getName());
        sb.append("\nlist=");
        sb.append(listKey.toString());
        sb.append("\n index =");
        sb.append(this.index);
        sb.append("\n item =");
        sb.append(this.item.toString());

        return sb.toString();
    }
}
