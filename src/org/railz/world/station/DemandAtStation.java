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

/** This class represents the demand for cargo at a station. */
public class DemandAtStation implements FreerailsSerializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2443599488012964106L;
	private final boolean[] demand;

	public DemandAtStation(boolean[] demand) {
		this.demand = demand.clone(); // defensive copy.
	}

	public boolean isCargoDemanded(int cargoNumber) {
		return demand[cargoNumber];
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof DemandAtStation) {
			DemandAtStation test = (DemandAtStation) o;

			if (this.demand.length != test.demand.length) {
				return false;
			}

			for (int i = 0; i < demand.length; i++) {
				if (demand[i] != test.demand[i]) {
					return false;
				}
			}

			return true;
		} else {
			return false;
		}
	}
}
