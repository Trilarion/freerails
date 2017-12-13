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

import java.awt.Point;
import java.awt.Rectangle;

import org.railz.world.building.*;
import org.railz.world.player.*;
import org.railz.world.track.*;
import org.railz.world.top.*;

public class ChangeBuildingMove implements MapUpdateMove {
    private BuildingTile oldTile;
    private BuildingTile newTile;
    Point point;
    private FreerailsPrincipal principal;
    
    public Rectangle getUpdatedTiles() {
	return new Rectangle(point.x, point.y, 1, 1);
    }

    public ChangeBuildingMove(Point p, BuildingTile oldTile, BuildingTile
	    newTile, FreerailsPrincipal buildingOwner) {
	point = new Point(p);
	principal = buildingOwner;
	this.oldTile = oldTile;
	this.newTile = newTile;
    }
    
    public FreerailsPrincipal getPrincipal() {
	return principal;
    }

    public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
	if (!w.getTile(point).getOwner().equals(p))
	    return MoveStatus.moveFailed("You don't own this tile");

	BuildingTile bTile = w.getTile(point).getBuildingTile();
	if (((oldTile == null) && (bTile != null)) ||
		((oldTile != null) && !oldTile.equals(bTile))) {
	    return MoveStatus.moveFailed("Tile was changed by another player");
	}

	if (newTile != null) {
	    TrackTile tt = w.getTile(point).getTrackTile();
	    BuildingType bType = (BuildingType) w.get(KEY.BUILDING_TYPES,
		    newTile.getType(), Player.AUTHORITATIVE);
	    if (tt != null) {
	       if (!bType.isTrackLayoutValid
		    (tt.getTrackConfiguration()))
		   return MoveStatus.moveFailed("Illegal track layout");

	       if (!bType.canBuildHere(w, point))
		   return MoveStatus.moveFailed("Cannot build on this " +
			   "terrain");
	    }
	}

	return MoveStatus.MOVE_OK;
    }

    public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
	if (!w.getTile(point).getOwner().equals(p))
	    return MoveStatus.moveFailed("You don't own this tile");

	BuildingTile bTile = w.getTile(point).getBuildingTile();
	if (((newTile == null) && (bTile != null)) ||
		((newTile != null) && !newTile.equals(bTile))) {
	    return MoveStatus.MOVE_FAILED;
	}
	
	if (oldTile != null) {
	    TrackTile tt = w.getTile(point).getTrackTile();
	    BuildingType bType = (BuildingType) w.get(KEY.BUILDING_TYPES,
		    oldTile.getType(), Player.AUTHORITATIVE);
	    if (tt != null) {
	       if (!bType.isTrackLayoutValid (tt.getTrackConfiguration()))
		   return MoveStatus.moveFailed("Illegal track layout");

	       if (!bType.canBuildHere(w, point))
		   return MoveStatus.moveFailed("Cannot build on this " +
			   "terrain");
	    }
	}

	return MoveStatus.MOVE_OK;
    }

    public MoveStatus doMove(World w, FreerailsPrincipal p) {
	MoveStatus ms = tryDoMove(w, p);
	if (ms != MoveStatus.MOVE_OK)
	    return ms;

	w.setTile(point.x, point.y, new FreerailsTile(w.getTile(point),
		    newTile));
	System.out.println("Did move " + this);
	return MoveStatus.MOVE_OK;
    }

    public MoveStatus undoMove(World w, FreerailsPrincipal p) {
	MoveStatus ms = tryUndoMove(w, p);
	if (ms != MoveStatus.MOVE_OK)
	    return ms;

	w.setTile(point.x, point.y, new FreerailsTile(w.getTile(point),
		    oldTile));
	return MoveStatus.MOVE_OK;
    }

    public boolean equals(Object o) {
	if (o == null || ! (o instanceof ChangeBuildingMove))
	    return false;

	ChangeBuildingMove m = (ChangeBuildingMove) o;

	return (((oldTile == null) ? (m.oldTile == null) :
		oldTile.equals(m.oldTile)) &&
		((newTile == null) ? (m.newTile == null) :
		 newTile.equals(m.newTile)) &&
		point.equals(m.point));
    }

    public int hashCode() {
	return point.hashCode();
    }

    public String toString() {
	return "ChangeBuildingMove " + super.toString() + ": oldTile=" +
	    oldTile + ", newTile=" + newTile + ", point=" + point + 
	    ", principal=" + principal;
    }
}

