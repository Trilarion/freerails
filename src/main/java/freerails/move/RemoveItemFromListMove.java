/*
 * Created on 13-Apr-2003
 *
 */
package freerails.move;

import freerails.world.common.FreerailsSerializable;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.top.KEY;
import freerails.world.top.World;

/**
 * All moves that remove an item from a list should extend this class.
 *
 * @author Luke
 */
public class RemoveItemFromListMove implements ListMove {
    private static final long serialVersionUID = 3906091169698953521L;

    private final FreerailsSerializable item;

    private final KEY listKey;

    private final int index;

    private final FreerailsPrincipal principal;

    RemoveItemFromListMove(KEY k, int i, FreerailsSerializable item,
                           FreerailsPrincipal p) {
        this.item = item;
        this.listKey = k;
        this.index = i;
        this.principal = p;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public int hashCode() {
        int result;
        result = (item != null ? item.hashCode() : 0);
        result = 29 * result + listKey.hashCode();
        result = 29 * result + index;
        result = 29 * result + principal.hashCode();

        return result;
    }

    public KEY getKey() {
        return listKey;
    }

    public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
        if (w.size(principal, listKey) < (index + 1)) {
            return MoveStatus.moveFailed("w.size(listKey)="
                    + w.size(principal, listKey) + " but index =" + index);
        }

        FreerailsSerializable item2remove = w.get(principal, listKey, index);

        if (null == item2remove) {
            return MoveStatus.moveFailed("The item at position " + index
                    + " has already been removed.");
        }

        if (!item.equals(item2remove)) {
            String reason = "The item at position " + index + " in the list ("
                    + item2remove.toString() + ") is not the expected item ("
                    + item.toString() + ").";

            return MoveStatus.moveFailed(reason);
        }
        return MoveStatus.MOVE_OK;
    }

    public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
        if (w.size(principal, listKey) < (index + 1)) {
            return MoveStatus.moveFailed("w.size(listKey)="
                    + w.size(principal, listKey) + " but index =" + index);
        }

        if (null != w.get(principal, listKey, index)) {
            String reason = "The item at position " + index + " in the list ("
                    + w.get(principal, listKey, index).toString()
                    + ") is not the expected item (null).";

            return MoveStatus.moveFailed(reason);
        }
        return MoveStatus.MOVE_OK;
    }

    public MoveStatus doMove(World w, FreerailsPrincipal p) {
        MoveStatus ms = tryDoMove(w, p);

        if (ms.isOk()) {
            w.set(principal, listKey, index, null);
        }

        return ms;
    }

    public MoveStatus undoMove(World w, FreerailsPrincipal p) {
        MoveStatus ms = tryUndoMove(w, p);

        if (ms.isOk()) {
            w.set(principal, listKey, index, this.item);
        }

        return ms;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof RemoveItemFromListMove) {
            RemoveItemFromListMove test = (RemoveItemFromListMove) o;

            if (!this.item.equals(test.getBefore())) {
                return false;
            }

            if (this.index != test.index) {
                return false;
            }

            return this.listKey == test.listKey;
        }
        return false;
    }

    public FreerailsSerializable getBefore() {
        return item;
    }

    public FreerailsSerializable getAfter() {
        return null;
    }

    public FreerailsPrincipal getPrincipal() {
        return principal;
    }
}