/*
 * Copyright (C) 2004 Robert Tuck
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
package org.railz.move;

import org.railz.world.common.*;
import org.railz.world.player.*;
import org.railz.world.top.*;
public class ChangeCalendarMove implements Move {
    private GameCalendar before;
    private GameCalendar after;

    public FreerailsPrincipal getPrincipal() {
	return Player.AUTHORITATIVE;
    }

    public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
	GameCalendar gc = (GameCalendar) w.get(ITEM.CALENDAR,
		Player.AUTHORITATIVE);
	return gc.equals(before) ? MoveStatus.MOVE_OK :
	    MoveStatus.MOVE_FAILED;
    }

    public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
	GameCalendar gc = (GameCalendar) w.get(ITEM.CALENDAR,
		Player.AUTHORITATIVE);
	return gc.equals(after) ? MoveStatus.MOVE_OK :
	    MoveStatus.MOVE_FAILED;
    }

    public MoveStatus doMove(World w, FreerailsPrincipal p) {
	MoveStatus ms = tryDoMove(w, p);
	if (!ms.isOk())
	    return ms;

	w.set(ITEM.CALENDAR, after, Player.AUTHORITATIVE);
	return MoveStatus.MOVE_OK;
    }

    public MoveStatus undoMove(World w, FreerailsPrincipal p) {
	MoveStatus ms = tryUndoMove(w, p);
	if (!ms.isOk())
	    return ms;

	w.set(ITEM.CALENDAR, before, Player.AUTHORITATIVE);
	return MoveStatus.MOVE_OK;
    }

    public String toString() {
	return "ChangeCalendarMove: before = " + before + 
	    ", after = " + after;
    }

    public ChangeCalendarMove(GameCalendar oldCal, GameCalendar newCal) {
	before = oldCal;
	after = newCal;
    }
}
