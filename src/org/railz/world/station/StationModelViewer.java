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
package org.railz.world.station;

import org.railz.world.common.*;
import org.railz.world.player.*;
import org.railz.world.top.*;
import org.railz.world.track.*;

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
     * TODO factor in influences such as current economy state, current year,
     * size of station, etc.
     */
    public long getImprovementCost(int improvementID) {
	StationImprovement si = (StationImprovement)
	    world.get(KEY.STATION_IMPROVEMENTS, improvementID,
		    Player.AUTHORITATIVE);
	return si.getBasePrice();
    }

    /**
     * Stations depreciate from their initial value over 25 years to 50% of
     * their initial value.
     * TODO factor in price of station improvements.
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

	long initialPrice = ((TrackRule) world.get(KEY.TRACK_RULES,
		    tile.getTrackRule(), Player.AUTHORITATIVE)).getPrice();
	if (elapsedYears >= 25) {
	    return (long) (initialPrice * 0.50);
	}
	return (long) (initialPrice * (1.0 - (elapsedYears * 0.5 / 25)));
    }

    public boolean canBuildImprovement(int improvementID) {
	// does this station already have the improvement 
	int[]  currentImprovements = stationModel.getImprovements();
	
	for (int i = 0; i < currentImprovements.length; i++) {
	    if (currentImprovements[i] == improvementID)
		return false;
	}

	StationImprovement si = (StationImprovement)
	    world.get(KEY.STATION_IMPROVEMENTS, improvementID,
		    Player.AUTHORITATIVE);

	// does this station have all necessary prerequisites
	int[] requiredImprovements = si.getPrerequisites();
	
	for (int i = 0; i < requiredImprovements.length; i++) {
	    boolean found = false;
	    for (int j = 0; j < currentImprovements.length; j++) {
		if (currentImprovements[j] == requiredImprovements[i]) {
		    found = true;
		    break;
		}
	    }
	    if (! found)
		return false;
	}
	return true;
    }
}
