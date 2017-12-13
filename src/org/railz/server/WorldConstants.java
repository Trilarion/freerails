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
package org.railz.server;

import org.railz.world.station.*;
import org.railz.world.top.*;
import org.railz.world.player.*;

class WorldConstants {
    public static int SI_WATER_TOWER;

    public static void init(ReadOnlyWorld w) {
	NonNullElements i = new NonNullElements(KEY.STATION_IMPROVEMENTS, w,
		Player.AUTHORITATIVE);
	while (i.next()) {
	    StationImprovement si = (StationImprovement) i.getElement();
	    if (si.getName().equals("WaterTower")) {
		SI_WATER_TOWER = i.getIndex();
	    }
	}
    }
}
