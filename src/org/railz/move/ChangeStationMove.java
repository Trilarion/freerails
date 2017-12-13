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

import org.railz.world.player.FreerailsPrincipal;
import org.railz.world.station.StationModel;
import org.railz.world.top.KEY;

/**
 *
 * This Move changes the properties of a station.
 *
 * @author lindsal
 */
final public class ChangeStationMove extends ChangeItemInListMove {
    public ChangeStationMove(int index, StationModel before, StationModel
	    after, FreerailsPrincipal p) {
        super(KEY.STATIONS, index, before, after, p);
    }
}
