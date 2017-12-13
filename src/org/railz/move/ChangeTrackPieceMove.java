/*
 * Copyright (C) Luke Lindsay
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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import org.railz.world.building.*;
import org.railz.world.player.FreerailsPrincipal;
import org.railz.world.player.Player;
import org.railz.world.terrain.TerrainType;
import org.railz.world.top.KEY;
import org.railz.world.top.World;
import org.railz.world.track.*;

/**
 * This Move adds, removes, or upgrades the track on a single tile.
 * @author Luke
 *
 */
final public class ChangeTrackPieceMove implements TrackMove, MapUpdateMove {
    final TrackTile trackPieceBefore;
    final TrackTile trackPieceAfter;
    final FreerailsPrincipal trackOwner;
    final Point location;

    public FreerailsPrincipal getPrincipal() {
	return trackOwner;
    }

    public Point getLocation() {
        return location;
    }

    public TrackTile getOldTrackPiece() {
        return trackPieceBefore;
    }

    public TrackTile getNewTrackPiece() {
        return trackPieceAfter;
    }

    public ChangeTrackPieceMove(TrackTile before, TrackTile after, Point p,
	    FreerailsPrincipal trackOwner) {
	if ((before != null && before.equals(after)) ||
		before == null && after == null)
	    throw new IllegalArgumentException(); 

        trackPieceBefore = before;
        trackPieceAfter = after;
	this.trackOwner = trackOwner;
        location = new Point(p);
    }

    public MoveStatus tryDoMove(World w, FreerailsPrincipal p) {
        return tryMove(w, this.trackPieceBefore, this.trackPieceAfter, p);
    }

    private MoveStatus tryMove(World w, TrackTile oldTrackPiece,
        TrackTile newTrackPiece, FreerailsPrincipal p) {
        //Check that location is on the map.
        if (!w.boundsContain(location.x, location.y)) {
            return MoveStatus.moveFailed(
                "Tried to build track outside the map.");
        }
	FreerailsTile currentTile = 
	    (FreerailsTile)w.getTile(location.x, location.y);

	if (! currentTile.getOwner().equals(trackOwner))
	    return MoveStatus.moveFailed("You don't own this track");

        //Check that the current track piece at this.location is
        //the same as this.oldTrackPiece.
	TrackTile currentTrackPieceAtLocation = currentTile.getTrackTile();
	if ((oldTrackPiece == null && currentTrackPieceAtLocation != null) ||
		((oldTrackPiece != null) &&
		 !oldTrackPiece.equals(currentTrackPieceAtLocation))) {
            return MoveStatus.moveFailed("Somebody else changed the track " +
		    "piece.");
        }

	// Check that the track is not locked by a train
	if (currentTrackPieceAtLocation != null &&
		currentTrackPieceAtLocation.isLocked()) {
	    return MoveStatus.moveFailed("A train is using this track.");
	}

	if (oldTrackPiece != null) {
	    TrackRule oldTrackRule = (TrackRule) w.get(KEY.TRACK_RULES,
		    oldTrackPiece.getTrackRule(), Player.AUTHORITATIVE);
	    if (!oldTrackRule.testTrackPieceLegality
		    (oldTrackPiece.getTrackConfiguration()))
		    return MoveStatus.moveFailed("Illegal track " +
			    "configuration.");
	}
	if (newTrackPiece != null) {
	    TrackRule newTrackRule = (TrackRule) w.get(KEY.TRACK_RULES,
		    newTrackPiece.getTrackRule(), Player.AUTHORITATIVE);
	    if (!newTrackRule.testTrackPieceLegality
		    (newTrackPiece.getTrackConfiguration()))
		    return MoveStatus.moveFailed("Illegal track " +
			    "configuration.");
	    TerrainType tt = (TerrainType) w.get(KEY.TERRAIN_TYPES,
		    currentTile.getTerrainTypeNumber(), Player.AUTHORITATIVE);
	    if (!newTrackRule.canBuildOnThisTerrainType
		    (tt.getTerrainCategory())) {
		String thisTrackType = newTrackRule.toString();
		int terrainCategory = tt.getTerrainCategory();

		return MoveStatus.moveFailed("Can't build " + thisTrackType +
			" on " + terrainCategory);
	    }
	    BuildingTile bTile = currentTile.getBuildingTile();
	    if (bTile != null) {
		BuildingType bType = (BuildingType) w.get(KEY.BUILDING_TYPES,
			bTile.getType(), Player.AUTHORITATIVE);
		if (!bType.isTrackLayoutValid
			(newTrackPiece.getTrackConfiguration()))
		    return MoveStatus.moveFailed("Illegal track layout");
	    }
	}
	
        return MoveStatus.MOVE_OK;
    }

    public MoveStatus tryUndoMove(World w, FreerailsPrincipal p) {
        return tryMove(w, this.trackPieceAfter, this.trackPieceBefore, p);
    }

    public MoveStatus doMove(World w, FreerailsPrincipal p) {
        MoveStatus moveStatus = tryDoMove(w, p);

        if (!moveStatus.isOk()) {
            return moveStatus;
        } else {
            move(w, this.trackPieceBefore, this.trackPieceAfter, p);

            return moveStatus;
        }
    }

    private void move(World w, TrackTile oldTrackPiece,
        TrackTile newTrackPiece, FreerailsPrincipal p) {
        FreerailsTile oldTile = w.getTile(location.x, location.y);
        FreerailsTile newTile = new FreerailsTile(oldTile, newTrackPiece);
        w.setTile(location.x, location.y, newTile);
    }

    public MoveStatus undoMove(World w, FreerailsPrincipal p) {
        MoveStatus moveStatus = tryUndoMove(w, p);

        if (!moveStatus.isOk()) {
            return moveStatus;
        } else {
            move(w, this.trackPieceAfter, this.trackPieceBefore, p);

            return moveStatus;
        }
    }

    public Rectangle getUpdatedTiles() {
        int x;
        int y;
        int width;
        int height;

        x = location.x - 1;
        y = location.y - 1;
        width = 3;
        height = 3;

        return new Rectangle(x, y, width, height);
    }

    public boolean equals(Object o) {
        if (o instanceof ChangeTrackPieceMove) {
            ChangeTrackPieceMove m = (ChangeTrackPieceMove)o;
            boolean fieldPointEqual = this.location.equals(m.location);
            boolean fieldoldTrackPieceEqual = 
		((trackPieceBefore == null && m.trackPieceBefore == null) ||
		 (trackPieceBefore != null && trackPieceBefore.equals
		  (m.trackPieceBefore)));
            boolean fieldnewTrackPieceEqual =
	       ((trackPieceAfter == null && m.trackPieceAfter == null) ||
	(trackPieceAfter != null &&
	 trackPieceAfter.equals(m.trackPieceAfter)));

            if (fieldPointEqual && fieldoldTrackPieceEqual &&
                    fieldnewTrackPieceEqual &&
		    m.trackOwner.equals(trackOwner)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public String toString() {
	return "ChangeTrackPieceMove: before=" + trackPieceBefore + ", after="
	    + trackPieceAfter + ", owner=" + trackOwner + ", location=" +
	    location;
    }
}
