package jfreerails.controller;

import java.util.Vector;

import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.NonNullElements;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.WorldIterator;

/**
 * Class to verify that the chosen name for a station hasn't already been taken
 * by another station. If the name has been used, a minor alteration in the name
 * is required, by adding perhaps "Junction" or "Siding" to the name.
 * 
 * @author Scott Bennett
 * 
 * Date: 12th April 2003
 * 
 */
public class VerifyStationName {
	private final ReadOnlyWorld w;

	private final String nameToVerify;

	private final Vector<String> stationAlternatives;

	public VerifyStationName(ReadOnlyWorld world, String name) {
		this.w = world;
		this.nameToVerify = name;
		this.stationAlternatives = new Vector<String>();

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

		// if (w.size(KEY.STATIONS) <= 0) {
		// //if there are no stations, then obviously the name isn't taken
		// return appropriateName;
		// }
		found = checkStationExists(appropriateName);

		if (!found) {
			return appropriateName;
		}
		// a station with that name already exists, so we need to find another
		// name
		for (int i = 0; i < stationAlternatives.size(); i++) {
			tempName = appropriateName + " " + stationAlternatives.elementAt(i);

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
		String testName = name;
		StationModel tempStation;

		for (int i = 0; i < w.getNumberOfPlayers(); i++) {
			FreerailsPrincipal principal = w.getPlayer(i).getPrincipal();

			WorldIterator wi = new NonNullElements(KEY.STATIONS, w, principal);

			while (wi.next()) { // loop over non null stations
				tempStation = (StationModel) wi.getElement();

				if ((testName).equals(tempStation.getStationName())) {
					// station already exists with that name
					return true;
				}
			}
		}

		// no stations exist with that name
		return false;
	}
}