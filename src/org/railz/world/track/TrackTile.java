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
package org.railz.world.track;

import java.awt.Point;

import org.railz.world.common.*;
import org.railz.world.player.Player;
import org.railz.world.top.*;

/**
 * The properties of a given tile which are specific to the track layer
 * @author rtuck99@users.berlios.de
 */
public abstract class TrackTile implements FreerailsSerializable {
    /**
     * Defines the number of Deltas per track tile. One track tile is defined
     * to be this many "Deltas" in width / height. A Delta is a
     * display-independent measurement of length. This allows clients to
     * calculate fine positions regardless of zoom-level etc. Pick an odd
     * number since integer division offsets the centre-point from the
     * middle and makes northeasterly diagonals occupy the correct tiles along
     * their length.
     */
    public static final int DELTAS_PER_TILE = 31;

    protected byte trackLayout;

    /**
     * The trackLock is used to control access to track. In order to traverse
     * a track square, the train must acquire a lock for the track, which is
     * dependent upon the directions the train is to travel in
     */
    protected transient byte trackLock;

    /**
     * An index into the TRACK_RULES database
     */
    private int trackType;

    /**
     * Attempt to acquire the track lock for the specified directions
     * @param directions specifies the directions of travel the train will
     * take when traversing the tile.
     * @return true if the lock could be acquired.
     */
    public abstract boolean getLock(byte directions);

    /**
     * Release the track lock for the specified directions
     */
    public abstract void releaseLock(byte directions);

    /**
     * @return an index into the TRACK_RULES database
     */
    public int getTrackRule() {
	return trackType;
    }

    public byte getTrackConfiguration() {
	return trackLayout;
    }

    public static TrackTile createTrackTile(ReadOnlyWorld w, byte trackLayout,
	    int trackType) {
	TrackRule tr = (TrackRule) w.get(KEY.TRACK_RULES, trackType,
		Player.AUTHORITATIVE);
	if (tr.isDoubleTrack()) {
	    return new DoubleTrackTile(trackLayout, trackType);
	}
	return new SingleTrackTile(trackLayout, trackType);
    }

    protected TrackTile(byte trackLayout, int trackType) {
	this.trackLayout = trackLayout;
	this.trackType = trackType;
    }

    /**
     * @return a new point at the centre of the map tile
     */
    public static final Point tileCoordsToDeltas(Point p) {
	Point newP = new Point((p.x * TrackTile.DELTAS_PER_TILE +
		    DELTAS_PER_TILE / 2),
		(p.y * TrackTile.DELTAS_PER_TILE + DELTAS_PER_TILE / 2));
	return newP;
    }

    public static final void deltasToTileCoords(Point p) {
	p.x /= DELTAS_PER_TILE;
	p.y /= DELTAS_PER_TILE;
    }

    public static final boolean deltaCoordInMapTile(Point deltaCoord, Point
	    mapTile) {
	return (deltaCoord.x >= mapTile.x * TrackTile.DELTAS_PER_TILE &&
		deltaCoord.x < (mapTile.x + 1) * TrackTile.DELTAS_PER_TILE &&
		deltaCoord.y >= mapTile.y * TrackTile.DELTAS_PER_TILE &&
		deltaCoord.y < (mapTile.y + 1) * TrackTile.DELTAS_PER_TILE);
    }

    /**
     * @return true if the tile is locked by a train passing over it
     */
    public boolean isLocked() {
	return trackLock != 0;
    }

    public boolean equals(Object o) {
	if (o == null || !(o instanceof TrackTile))
	    return false;
	TrackTile tt = (TrackTile) o;
	
	return (trackLayout == tt.trackLayout &&
		trackType == tt.trackType);
    }

    public int hashCode() {
	return trackLayout ^ trackType;
    }
}
