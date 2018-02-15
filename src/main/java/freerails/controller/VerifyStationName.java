/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package freerails.controller;

import freerails.model.world.WorldKey;
import freerails.model.NonNullElementWorldIterator;
import freerails.model.world.ReadOnlyWorld;
import freerails.model.WorldIterator;
import freerails.model.player.FreerailsPrincipal;
import freerails.model.station.Station;

import java.util.LinkedList;

/**
 * Class to verify that the chosen name for a station hasn't already been taken
 * by another station. If the name has been used, a minor alteration in the name
 * is required, by adding perhaps "Junction" or "Siding" to the name.
 */
public class VerifyStationName {

    private final ReadOnlyWorld world;
    private final String nameToVerify;
    private final LinkedList<String> stationAlternatives;

    /**
     * @param world
     * @param name
     */
    public VerifyStationName(ReadOnlyWorld world, String name) {
        this.world = world;
        nameToVerify = name;
        stationAlternatives = new LinkedList<>();

        stationAlternatives.add("Junction");
        stationAlternatives.add("Siding");
        stationAlternatives.add("North");
        stationAlternatives.add("East");
        stationAlternatives.add("South");
        stationAlternatives.add("West");
    }

    /**
     * @return
     */
    public String getName() {
        String appropriateName = nameToVerify;
        boolean found;
        String tempName = null;

        found = checkStationExists(appropriateName);

        if (!found) {
            return appropriateName;
        }
        // a station with that name already exists, so we need to find another
        // name
        for (String stationAlternative : stationAlternatives) {
            tempName = appropriateName + ' ' + stationAlternative;
            found = checkStationExists(tempName);
            if (!found) {
                return tempName;
            }
        }

        int j = 7; // for number of names that have already been used

        while (found) {
            j++;
            tempName = appropriateName + "Station #" + j;
            found = checkStationExists(tempName);
        }

        return tempName;
    }

    private boolean checkStationExists(String name) {
        Station tempStation;

        for (int i = 0; i < world.getNumberOfPlayers(); i++) {
            FreerailsPrincipal principal = world.getPlayer(i).getPrincipal();

            WorldIterator wi = new NonNullElementWorldIterator(WorldKey.Stations, world, principal);

            while (wi.next()) { // loop over non null stations
                tempStation = (Station) wi.getElement();

                if ((name).equals(tempStation.getStationName())) {
                    // station already exists with that name
                    return true;
                }
            }
        }
        // no stations exist with that name
        return false;
    }
}