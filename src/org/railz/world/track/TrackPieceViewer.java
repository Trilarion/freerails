/*
 * Copyright (C) Robert Tuck
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

/**
 * @author rtuck99@users.belios.de
 */
package org.railz.world.track;

import org.railz.world.common.*;
import org.railz.world.player.*;
import org.railz.world.top.*;

public class TrackPieceViewer implements FixedAsset {
    private ReadOnlyWorld world;
    private int x, y;
    private FreerailsTile tile;

    public TrackPieceViewer(ReadOnlyWorld w) {
	world = w;
    }

    public void setFreerailsTile(int x, int y) {
	tile = world.getTile(x, y);
    }

    /**
     * Track is valued at 25% of initial cost, irrespective of age.
     * TODO
     * Stations have zero value since station and track are indivisible.
     * Stations are accounted for elsewhere.
     */
    public long getBookValue() {
	return (long) (((TrackRule) world.get(KEY.TRACK_RULES,
			tile.getTrackRule(), Player.AUTHORITATIVE)).getPrice()
		* 0.25);
    }
}
