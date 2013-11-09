/*
 * Copyright (C) 2003 Luke Lindsay
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

/*
 * Created on 27-Apr-2003
 *
 */
package org.railz.world.building;

import org.railz.world.common.FreerailsSerializable;

/**
 * This class represents the production of a raw material on a tile.
 * 
 * @author Luke
 * 
 */
public class Production implements FreerailsSerializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -349050370796821251L;

	private final int cargoType;

	/**
	 * The number of tonnes per year
	 */
	private final int rate;

	public Production(int type, int rate) {
		this.cargoType = type;
		this.rate = rate;
	}

	/**
	 * @return An index into the CARGO_TYPES table for the cargo type for which
	 *         this Production object measures production
	 */
	public int getCargoType() {
		return cargoType;
	}

	/**
	 * @return the rate of production of the cargo type in tonnes per year
	 */
	public int getRate() {
		return rate;
	}
}
