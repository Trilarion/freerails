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
 * Created on 23-Mar-2003
 *
 */
package org.railz.server;

import org.railz.world.cargo.CargoType;
import org.railz.world.train.EngineType;
import org.railz.world.train.WagonType;
import org.railz.world.player.Player;
import org.railz.world.top.*;

/**
 * This class adds hard coded wagon types to the World.  Later the
 * wagon types will be defined in an xml file, but this will do for
 * now.
 *
 * @author Luke
 *
 */
public class WagonAndEngineTypesFactory {
    private static final int UNITS_OF_CARGO_PER_WAGON = 40;

    public void addTypesToWorld(World w) {
        //Wagon types
	/*
	 * Create a wagon type for each cargo type
	 * XXX correspondence between cargo type and WagonType table index will
	 * not be guaranteed in future XXX
	 */
	int s = w.size(KEY.CARGO_TYPES);
	WagonType[] wagonTypes = new WagonType[s];
	for (int i = 0; i < s; i++) {
	    CargoType ct = (CargoType) w.get(KEY.CARGO_TYPES, i);
	    wagonTypes[i] = new WagonType(ct.getName(), ct.getCategory(),
		    UNITS_OF_CARGO_PER_WAGON, i);
	}

        for (int i = 0; i < wagonTypes.length; i++) {
            w.add(KEY.WAGON_TYPES, wagonTypes[i], Player.AUTHORITATIVE);
        }
    }
}
