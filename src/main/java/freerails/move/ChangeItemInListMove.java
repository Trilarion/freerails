/*
 * Created on 13-Apr-2003
 *
 */
package freerails.move;

import freerails.util.Utils;
import freerails.world.common.FreerailsSerializable;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.top.KEY;
import freerails.world.top.World;

/**
 * All Moves that replace an item in a list with another should extend this
 * class.
 *
 * @author Luke
 */
public class ChangeItemInListMove implements ListMove {
    private static final long serialVersionUID = -4457694821370844051L;

    private final KEY listKey;

    private final int index;

    private final FreerailsSerializable before;

    private final FreerailsSerializable after;

    private final FreerailsPrincipal principal;

    /**
     *
     * @param k
     * @param index
     * @param before
     * @param after
     * @param p
     */
    public ChangeItemInListMove(KEY k, int index, FreerailsSerializable before,
                                FreerailsSerializable after, FreerailsPrincipal p) {
        this.before = before;
        this.after = after;
        this.index = index;
        this.listKey = k;
        this.principal = p;
    }

    /**
     *
     * @return
     */
    public boolean beforeEqualsAfter() {
        return Utils.equal(this.before, this.after);
    }

    public MoveStatus doMove(World w, FreerailsPrincipal p) {
        return move(after, before, w);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ChangeItemInListMove) {
            ChangeItemInListMove test = (ChangeItemInListMove) o;

            if (!before.equals(test.getBefore())) {
                return false;
            }

            if (!after.equals(test.getAfter())) {
                return false;
            }

            if (index != test.index) {
                return false;
            }

            return listKey == test.listKey;
        }
        return false;
    }

    public FreerailsSerializable getAfter() {
        return after;
    }

    public FreerailsSerializable getBefore() {
        return before;
    }

    public int getIndex() {
        return index;
    }

    public KEY getKey() {
        return listKey;
    }

    /**
     *
     * @return
     */
    public FreerailsPrincipal getPrincipal() {
        return principal;
    }

    @Override
    public int hashCode() {
        int result;
        result = listKey.hashCode();
        result = 29 * result + index;
        result = 29 * result + (before != null ? before.hashCode() : 0);
        result = 29 * result + (after != null ? after.hashCode() : 0);
        result = 29 * result + principal.hashCode();

        return result;
    }

    /**
     *
     * @param to
     * @param from
     * @param w
     * @return
     */
    protected MoveStatus move(FreerailsSerializable to,
                              FreerailsSerializable from, World w) {
        MoveStatus ms = tryMove(to, from, w);

        if (ms.ok) {
            w.set(principal, listKey, index, to);
        }

        return ms;
    }

    @Override
    public String toString() {
        return getClass().getName() +
                " before: " +
                before.toString() +
                " after: " +
                after.toString();
    }

    public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
        return tryMove(after, before, w);
    }

    /**
     *
     * @param to
     * @param from
     * @param w
     * @return
     */
    protected MoveStatus tryMove(FreerailsSerializable to,
                                 FreerailsSerializable from, World w) {
        if (index >= w.size(principal, listKey)) {
            return MoveStatus.moveFailed("w.size(listKey) is "
                    + w.size(principal, listKey) + " but index is " + index);
        }

        FreerailsSerializable item2change = w.get(principal, listKey, index);

        if (null == item2change) {
            if (null == from) {
                return MoveStatus.MOVE_OK;
            }
            return MoveStatus.moveFailed("Expected null but found " + from);
        }
        if (!from.equals(item2change)) {
            String message = "Expected " + from.toString() + " but found "
                    + to.toString();
            return MoveStatus.moveFailed(message);
        }
        return MoveStatus.MOVE_OK;
    }

    public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
        return tryMove(before, after, w);
    }

    public MoveStatus undoMove(World w, FreerailsPrincipal p) {
        return move(before, after, w);
    }
}