/*
 * Copyright (C) Scott Bennett
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
 * @author Scott Bennett
 *
 * Date: 12th April 2003
 *
 * Class to verify that the chosen name for a station hasn't already been
 * taken by another station. If the name
 * has been used, a minor alteration in the name is required, by adding
 * perhaps "Junction" or "Siding" to the name.
 *
 */
package org.railz.controller;

import java.util.Vector;
import org.railz.world.player.FreerailsPrincipal;
import org.railz.world.player.Player;
import org.railz.world.station.StationModel;
import org.railz.world.top.KEY;
import org.railz.world.top.NonNullElements;
import org.railz.world.top.ReadOnlyWorld;
import org.railz.world.top.WorldIterator;


public class VerifyStationName {
    private ReadOnlyWorld w;
    private String nameToVerify;
    private Vector stationAlternatives;

    public VerifyStationName(ReadOnlyWorld world, String name) {
        this.w = world;
        this.nameToVerify = name;
        this.stationAlternatives = new Vector();

        stationAlternatives.addElement("Junction");
        stationAlternatives.addElement("Siding");
        stationAlternatives.addElement("North");
        stationAlternatives.addElement("East");
        stationAlternatives.addElement("South");
        stationAlternatives.addElement("West");
    }

    public String getName() {
        String appropriateName = nameToVerify;
        boolean found = false;
        String tempName = null;

        found = checkStationExists(appropriateName);

        if (!found) {
            return appropriateName;
        } else {
            //a station with that name already exists, so we need to find another name
            for (int i = 0; i < stationAlternatives.size(); i++) {
                tempName = appropriateName + " " +
                    (String)stationAlternatives.elementAt(i);

                found = checkStationExists(tempName);

                if (!found) {
                    return tempName;
                }
            }

            int j = 7; //for number of names that have already been used

            while (found) {
                j++;
                tempName = appropriateName + "Station #" + j;
                found = checkStationExists(tempName);
            }

            return tempName;
        }
    }

    private boolean checkStationExists(String name) {
        String testName = name;
        StationModel tempStation;

	NonNullElements i = new NonNullElements(KEY.PLAYERS, w,
		Player.AUTHORITATIVE);
	while (i.next()) {
	    FreerailsPrincipal p = (FreerailsPrincipal) ((Player)
		    i.getElement()).getPrincipal();
	    WorldIterator wi = new NonNullElements(KEY.STATIONS, w, p);

	    while (wi.next()) { //loop over non null stations
		tempStation = (StationModel)wi.getElement();

		if ((testName).equals(tempStation.getStationName())) {
		    //station already exists with that name
		    return true;
		}
	    }
	}

        //no stations exist with that name	
        return false;
    }
}
