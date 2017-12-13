/*
 * Copyright (C) 2003 Luke Lindsay
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

/*
 * Created on 13-Apr-2003
 *
 */
package org.railz.move;

import org.railz.world.common.FreerailsSerializable;
import org.railz.world.top.KEY;
import org.railz.world.top.World;
import org.railz.world.player.FreerailsPrincipal;
import org.railz.world.player.Player;

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
                    " but found " + item2change.toString());
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

    public String toString() {
	return "ChangeItemInListMove: " + listKey + ", " + index + ", " +
	    principal + " - change from: " + before + " to: " + after;
    }
}
