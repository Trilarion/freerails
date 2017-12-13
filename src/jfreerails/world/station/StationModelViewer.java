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
package jfreerails.world.station;

import jfreerails.world.common.*;
import jfreerails.world.player.*;
import jfreerails.world.top.*;
import jfreerails.world.track.FreerailsTile;
public class StationModelViewer implements FixedAsset {
    private ReadOnlyWorld world;
    private StationModel stationModel;
    private GameCalendar calendar;

    public StationModelViewer(ReadOnlyWorld w) {
	world = w;
	calendar = (GameCalendar) world.get(ITEM.CALENDAR,
		Player.AUTHORITATIVE);
    }

    public void setStationModel(StationModel sm) {
	stationModel = sm;
    }

    /**
     * Stations depreciate from their initial value over 25 years to 50% of
     * their initial value.
     */
    public long getBookValue() {
	GameTime now = (GameTime) world.get(ITEM.TIME, Player.AUTHORITATIVE);
	long nowMillis = calendar.getCalendar(now).getTimeInMillis();
	long creationMillis = calendar.getCalendar
	    (stationModel.getCreationDate()).getTimeInMillis();
	/* assume years are 365 days long for simplicity */
	int elapsedYears = (int)
	    ((nowMillis - creationMillis) / (1000L * 60 * 60 * 24 * 365));
	FreerailsTile tile = world.getTile(stationModel.getStationX(),
		stationModel.getStationY());

	long initialPrice = tile.getTrackRule().getPrice();
	if (elapsedYears >= 25) {
	    return (long) (initialPrice * 0.50);
	}
	return (long) (initialPrice * (1.0 - (elapsedYears * 0.5 / 25)));
    }
}
