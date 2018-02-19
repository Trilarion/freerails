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

package freerails.model.station;

import freerails.model.world.PlayerKey;
import freerails.model.NonNullElementWorldIterator;
import freerails.model.world.ReadOnlyWorld;
import freerails.model.WorldIterator;
import freerails.model.player.FreerailsPrincipal;

import java.util.Arrays;
import java.util.List;

/**
 * Class to verify that the chosen name for a station hasn't already been taken
 * by another station. If the name has been used, a minor alteration in the name
 * is required, by adding perhaps "Junction" or "Siding" to the name.
 */
public class VerifyStationName {

    private final ReadOnlyWorld world;
    private final String nameToVerify;
    private final List<String> stationAlternatives = Arrays.asList("Junction", "Siding", "North", "East", "South", "West");

    /**
     * @param world
     * @param name
     */
    public VerifyStationName(ReadOnlyWorld world, String name) {
        this.world = world;
        nameToVerify = name;
    }

    /**
     * @return
     */
    public String getName() {
        String appropriateName = nameToVerify;
        boolean found;
        String tempName = null;

        found = existsStationName(appropriateName);

        if (!found) {
            return appropriateName;
        }
        // a station with that name already exists, so we need to find another
        // name
        for (String stationAlternative : stationAlternatives) {
            tempName = appropriateName + ' ' + stationAlternative;
            found = existsStationName(tempName);
            if (!found) {
                return tempName;
            }
        }

        int j = 7; // for number of names that have already been used

        while (found) {
            j++;
            tempName = appropriateName + "Station #" + j;
            found = existsStationName(tempName);
        }

        // TODO could it be we still don't have a valid one?
        return tempName;
    }

    private boolean existsStationName(String name) {
        for (int i = 0; i < world.getNumberOfPlayers(); i++) {
            FreerailsPrincipal principal = world.getPlayer(i).getPrincipal();

            WorldIterator worldIterator = new NonNullElementWorldIterator(PlayerKey.Stations, world, principal);

            while (worldIterator.next()) { // loop over non null stations
                Station station = (Station) worldIterator.getElement();

                if (name.equals(station.getStationName())) {
                    // station already exists with that name
                    return true;
                }
            }
        }
        // no stations exist with that name
        return false;
    }
}