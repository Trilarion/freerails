/*
 * Copyright (C) 2002 Luke Lindsay
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
 * @author Luke Lindsay 08-Nov-2002
 *
 * Updated 12th April 2003 by Scott Bennett to include nearest city names.
 *
 * Class to build a station at a given point, names station after nearest
 * city. If that name is taken then a "Junction" or "Siding" is added to
 * the name.
 */
package org.railz.controller;

import java.awt.Point;
import org.railz.move.*;
import org.railz.world.building.*;
import org.railz.world.common.*;
import org.railz.world.top.KEY;
import org.railz.world.top.ReadOnlyWorld;
import org.railz.world.track.FreerailsTile;
import org.railz.world.track.TrackRule;
import org.railz.world.player.*;

public class StationBuilder {
    private UntriedMoveReceiver moveReceiver;
    private ReadOnlyWorld w;
    private int ruleNumber;
    private TrackMoveTransactionsGenerator transactionsGenerator;
    private FreerailsPrincipal stationOwner;

    public StationBuilder(UntriedMoveReceiver moveReceiver,
        ReadOnlyWorld world, FreerailsPrincipal p) {
        this.moveReceiver = moveReceiver;
	stationOwner = p;
        w = world;

        BuildingType bType;

        int i = -1;

        do {
            i++;
            bType = (BuildingType)w.get(KEY.BUILDING_TYPES, i);
        } while (bType.getCategory() != BuildingType.CATEGORY_STATION);

        ruleNumber = i;
        transactionsGenerator = new TrackMoveTransactionsGenerator(w, p);
    }

    public boolean canBuiltStationHere(Point p) {
        FreerailsTile oldTile = w.getTile(p.x, p.y);

        if (oldTile.getTrackTile() == null)
	    return false;

	/* if there is a building present, it must be a station */
	BuildingTile bTile = oldTile.getBuildingTile();
	if (bTile != null) {
	   BuildingType bType = (BuildingType) w.get(KEY.BUILDING_TYPES,
		   bTile.getType(), Player.AUTHORITATIVE);
		if (bType.getCategory() != BuildingType.CATEGORY_STATION)
		    return false;
	}

	return true;
    }

    public void buildStation(Point p) {
        FreerailsTile oldTile = w.getTile(p.x, p.y);

        //Only build a station if there is track at the specified point.
        if (canBuiltStationHere(p)) {
	    System.err.println("Can build station");
            String cityName;
            String stationName;

	    BuildingTile bTile = oldTile.getBuildingTile();
	    BuildingType bType = null;
	    if (bTile != null) {
		bType = (BuildingType) w.get(KEY.BUILDING_TYPES,
			bTile.getType(), Player.AUTHORITATIVE);
	    }

	    if (bTile == null || bType.getCategory() !=
		    BuildingType.CATEGORY_STATION) {
		System.err.println("Adding new station");
		//There isn't already a station here, we need to pick a name
		//and add an entry to the station list.
		CalcNearestCity cNC = new CalcNearestCity(w, p.x, p.y);
		cityName = cNC.findNearestCity();

		VerifyStationName vSN = new VerifyStationName(w, cityName);
		stationName = vSN.getName();

		if (stationName == null) {
		    //there are no cities, this should never happen
		    stationName = "Central Station";
		}

		//check the terrain to see if we can build a station on it...
		Move m = AddStationMove.generateMove(w, stationName, p,
			stationOwner, ruleNumber);

		this.moveReceiver.processMove
		    (transactionsGenerator.addTransactions(m));
		System.err.println("move done");
	    } else {
		//Upgrade an existing station.
		ChangeBuildingMove cbm = new ChangeBuildingMove(p, bTile,
			new BuildingTile(ruleNumber), stationOwner);
		this.moveReceiver.processMove(cbm);
            }
        } else {
            System.err.println(
                "Can't build station here");
        }
    }

	/**
	 * @param ruleNumber an index into the BUILDING_TYPES table
	 */
    public void setStationType(int ruleNumber) {
        this.ruleNumber = ruleNumber;
    }
}
