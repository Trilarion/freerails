/*
 * Created on 13-Apr-2003
 *
 */
package jfreerails.move;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.KEY;
import jfreerails.world.top.World;


/**
 * All moves that add an item to a list should extend this class.
 *
 * @author Luke
 *
 */
public class AddItemToListMove implements ListMove {
    final KEY listKey;
    final int index;
    final FreerailsPrincipal principal;
    protected final FreerailsSerializable item;

    public int getIndex() {
        return index;
    }

    public KEY getKey() {
        return listKey;
    }

    protected AddItemToListMove(KEY key, int i, FreerailsSerializable item,
        FreerailsPrincipal p) {
        this.listKey = key;
        this.index = i;
        this.item = item;
        this.principal = p;
    }

    public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
        if (w.size(listKey, this.principal) != index) {
            return MoveStatus.moveFailed("Expected size of list is " + index +
                " but actual size is " + w.size(listKey, this.principal));
        }

        return MoveStatus.MOVE_OK;
    }

    public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
        int expectListSize = index + 1;

        if (w.size(listKey, this.principal) != expectListSize) {
            return MoveStatus.moveFailed("Expected size of list is " +
                expectListSize + " but actual size is " +
                w.size(listKey, this.principal));
        }

        return MoveStatus.MOVE_OK;
    }

    public MoveStatus doMove(World w, FreerailsPrincipal p) {
        MoveStatus ms = tryDoMove(w, p);

        if (ms.isOk()) {
            w.add(listKey, this.item, this.principal);
        }

        return ms;
    }

    public MoveStatus undoMove(World w, FreerailsPrincipal p) {
        MoveStatus ms = tryUndoMove(w, p);

        if (ms.isOk()) {
            w.removeLast(listKey, this.principal);
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

    public FreerailsPrincipal getPrincipal() {
        return principal;
    }
}