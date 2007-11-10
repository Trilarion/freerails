/*
 * Created on 13-Apr-2003
 *
 */
package jfreerails.move;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.SKEY;
import jfreerails.world.top.World;

/**
 * All moves that add an item to a shared list should extend this class.
 * 
 * @author Luke
 * 
 */
public class AddItemToSharedListMove implements Move {
    private static final long serialVersionUID = 3762256352759722807L;

    private final SKEY listKey;

    private final int index;

    private final FreerailsSerializable item;

    public int getIndex() {
        return index;
    }

    @Override
    public int hashCode() {
        int result;
        result = listKey.hashCode();
        result = 29 * result + index;
        result = 29 * result + (item != null ? item.hashCode() : 0);

        return result;
    }

    public SKEY getKey() {
        return listKey;
    }

    protected AddItemToSharedListMove(SKEY key, int i,
            FreerailsSerializable item) {
        this.listKey = key;
        this.index = i;
        this.item = item;
    }

    public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
        if (w.size(listKey) != index) {
            return MoveStatus.moveFailed("Expected size of "
                    + listKey.toString() + " list is " + index
                    + " but actual size is " + w.size(listKey));
        }

        return MoveStatus.MOVE_OK;
    }

    public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
        int expectListSize = index + 1;

        if (w.size(listKey) != expectListSize) {
            return MoveStatus.moveFailed("Expected size of "
                    + listKey.toString() + " list is " + expectListSize
                    + " but actual size is " + w.size(listKey));
        }

        return MoveStatus.MOVE_OK;
    }

    public MoveStatus doMove(World w, FreerailsPrincipal p) {
        MoveStatus ms = tryDoMove(w, p);

        if (ms.isOk()) {
            w.add(listKey, this.item);
        }

        return ms;
    }

    public MoveStatus undoMove(World w, FreerailsPrincipal p) {
        MoveStatus ms = tryUndoMove(w, p);

        if (ms.isOk()) {
            w.removeLast(listKey);
        }

        return ms;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof AddItemToSharedListMove) {
            AddItemToSharedListMove test = (AddItemToSharedListMove) o;

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
        }
        return false;
    }

    public FreerailsSerializable getBefore() {
        return null;
    }

    public FreerailsSerializable getAfter() {
        return item;
    }

    @Override
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