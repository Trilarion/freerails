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

import java.util.*;

import org.railz.world.common.*;
import org.railz.world.player.*;
import org.railz.world.player.Statistic.DataPoint;
import org.railz.world.top.*;
/**
 * Add a data point to a statistic
 */
public class AddDataPointMove implements Move {
    private GameTime time;
    int yValue;
    private ObjectKey objectKey;
    private int size;

    public AddDataPointMove(ObjectKey ok, GameTime t, int yValue, ReadOnlyWorld
	    w) {
	this.yValue = yValue;
	time = t;
	size = ((Statistic) w.get(KEY.STATISTICS, ok.index, ok.principal))
	    .getData().size();
	objectKey = ok;
    }

    public FreerailsPrincipal getPrincipal() {
	return objectKey.principal;
    }

    public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
	Statistic s = (Statistic) w.get(KEY.STATISTICS, objectKey.index,
		objectKey.principal);
	if (s.getData().size() != size)
	    return MoveStatus.MOVE_FAILED;

	return MoveStatus.MOVE_OK;
    }

    public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
	Statistic s = (Statistic) w.get(KEY.STATISTICS, objectKey.index,
		objectKey.principal);
	ArrayList l = s.getData();
	if (l.size() != size + 1)
	    return MoveStatus.MOVE_FAILED;

	DataPoint dp = (DataPoint) l.get(size);
	if (! dp.time.equals(time) ||
		dp.y != yValue)
	    return MoveStatus.MOVE_FAILED;

	return MoveStatus.MOVE_OK;
    }

    public MoveStatus doMove(World w, FreerailsPrincipal p) {
	if (tryDoMove(w, p) == MoveStatus.MOVE_OK) {
	    Statistic s = (Statistic) w.get(KEY.STATISTICS, objectKey.index,
		    objectKey.principal);
	    s.addDataPoint(time, yValue);
	    return MoveStatus.MOVE_OK;
	}
	return MoveStatus.MOVE_FAILED;
    }

    public MoveStatus undoMove(World w, FreerailsPrincipal p) {
	if (tryUndoMove(w, p) == MoveStatus.MOVE_OK) {
	    Statistic s = (Statistic) w.get(KEY.STATISTICS, objectKey.index,
		    objectKey.principal);
	    s.removeDataPoint();
	    return MoveStatus.MOVE_OK;
	}
	return MoveStatus.MOVE_FAILED;
    }
}
