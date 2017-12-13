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
 * @author rtuck99@users.berlios.de
 */
package jfreerails.world.terrain;

import jfreerails.world.player.Player;
import jfreerails.world.track.FreerailsTile;
import jfreerails.world.top.*;
import jfreerails.world.common.*;

/**
 * This class provides methods describing properties of the tile which require
 * context from the game world.
 */
public class TerrainTileViewer implements FixedAsset {
    private ReadOnlyWorld world;
    private FreerailsTile tile;
    private int x, y;

    public TerrainTileViewer(ReadOnlyWorld w) {
       world = w;
    }       

    public void setFreerailsTile(int x, int y) {
	tile = world.getTile(x, y);
	this.x = x;
	this.y = y;
    }

    /**
     * @return the asset value of a tile, excluding any buildings or track.
     */
    public long getBookValue() {
	return getTerrainValue();
    }

    /**
     * Calculates the value of the tile based on the base value of this tile,
     * adjusted by an aaverage of the values of the surrounding tiles.
     * TODO perform the averaging...
     * @return the purchase value of a tile, excluding any buildings or track.
     */
    public long getTerrainValue() {
	TerrainType t = (TerrainType) world.get(KEY.TERRAIN_TYPES,
		tile.getTerrainTypeNumber(), Player.AUTHORITATIVE);

	return t.getBaseValue();
    }
}
