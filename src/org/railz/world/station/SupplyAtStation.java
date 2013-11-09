/*
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

package org.railz.world.station;

import org.railz.world.common.FreerailsSerializable;

/** This class represents the supply at a station. */
public class SupplyAtStation implements FreerailsSerializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8263231042433535122L;
	private final int[] supply;

	public SupplyAtStation(int[] cargoWaiting) {
		supply = cargoWaiting.clone();
	}

	/**
	 * Returns the number of car loads of the specified cargo that the station
	 * supplies per year.
	 */
	public int getSupply(int cargoType) {
		return supply[cargoType];
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof SupplyAtStation) {
			SupplyAtStation test = (SupplyAtStation) o;

			if (this.supply.length != test.supply.length) {
				return false;
			}

			for (int i = 0; i < supply.length; i++) {
				if (supply[i] != test.supply[i]) {
					return false;
				}
			}

			return true;
		} else {
			return false;
		}
	}
}
