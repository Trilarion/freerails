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
 * All Moves that replace an item in a list with another should extend this class.
 *
 * @author Luke
 *
 */
public abstract class ChangeItemInListMove implements ListMove {
    private final KEY m_listKey;
    private final int m_index;
    private final FreerailsSerializable m_before;
    private final FreerailsSerializable m_after;
    private final FreerailsPrincipal m_principal;

    public int getIndex() {
        return m_index;
    }

    public int hashCode() {
        int result;
        result = m_listKey.hashCode();
        result = 29 * result + m_index;
        result = 29 * result + (m_before != null ? m_before.hashCode() : 0);
        result = 29 * result + (m_after != null ? m_after.hashCode() : 0);
        result = 29 * result + m_principal.hashCode();

        return result;
    }

    public KEY getKey() {
        return m_listKey;
    }

    protected ChangeItemInListMove(KEY k, int index,
        FreerailsSerializable before, FreerailsSerializable after,
        FreerailsPrincipal p) {
        m_before = before;
        m_after = after;
        m_index = index;
        m_listKey = k;
        m_principal = p;
    }

    public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
        return tryMove(m_after, m_before, w);
    }

    public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
        return tryMove(m_before, m_after, w);
    }

    public MoveStatus doMove(World w, FreerailsPrincipal p) {
        return move(m_after, m_before, w);
    }

    public MoveStatus undoMove(World w, FreerailsPrincipal p) {
        return move(m_before, m_after, w);
    }

    protected MoveStatus tryMove(FreerailsSerializable to,
        FreerailsSerializable from, World w) {
        if (m_index >= w.size(m_listKey, m_principal)) {
            return MoveStatus.moveFailed("w.size(listKey) is " +
                w.size(m_listKey, m_principal) + " but index is " + m_index);
        }

        FreerailsSerializable item2change = w.get(m_listKey, m_index,
                m_principal);

        if (null == item2change) {
            if (null == from) {
                return MoveStatus.MOVE_OK;
            } else {
                return MoveStatus.moveFailed("Expected null but found " + from);
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
        FreerailsSerializable from, World w) {
        MoveStatus ms = tryMove(to, from, w);

        if (ms.ok) {
            w.set(m_listKey, m_index, to, m_principal);
        }

        return ms;
    }

    public boolean equals(Object o) {
        if (o instanceof ChangeItemInListMove) {
            ChangeItemInListMove test = (ChangeItemInListMove)o;

            if (!m_before.equals(test.getBefore())) {
                return false;
            }

            if (!m_after.equals(test.getAfter())) {
                return false;
            }

            if (m_index != test.m_index) {
                return false;
            }

            if (m_listKey != test.m_listKey) {
                return false;
            }

            return true;
        } else {
            return false;
        }
    }

    public FreerailsSerializable getAfter() {
        return m_after;
    }

    public FreerailsSerializable getBefore() {
        return m_before;
    }

    public FreerailsPrincipal getPrincipal() {
        return m_principal;
    }
}