/*
 * Copyright (C) Luke Lindsay
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

package org.railz.world.train;

import org.railz.world.common.FreerailsSerializable;

/**
 * This class represents an engine type, for example 'Grass Hopper'.  It
 * encapsulates the properties that are common to all engines of the same
 * type.
 *
 * @author Luke
 */
final public class EngineType implements FreerailsSerializable {
    public static final int FUEL_TYPE_COAL = 1;
    public static final int FUEL_TYPE_DIESEL = 2;
    public static final int FUEL_TYPE_ELECTRIC = 3;

    /**
     * This is the conversion factor between "real" units and game-world ones
     */
    public static final int TILE_HOURS_PER_MILE_BIGTICKS = 25;

    private final String engineTypeName;
    private final int fuelType;
    private final long price;
    private final long maintenance;
    private final int maxSpeed; //speed in mph
    private final int waterCapacity; // waterCapacity in units

    /** fuel consumption in units per year */
    private final int annualFuelConsumption;

    /**
     * @return annual maintenance expense
     */
    public long getMaintenance() {
        return maintenance;
    }

    public String getEngineTypeName() {
        return engineTypeName;
    }

    public long getPrice() {
        return price;
    }

    public int getMaxSpeed() {
        return maxSpeed;
    }

    public void getRatedTrainSpeedAtGrade(int speed, int grade) {
    }

    public EngineType(String name, long m, int speed,
        long maintenance, int annualFuelConsumption, int fuelType,
	int waterCapacity) {
        engineTypeName = name;
        price = m;
        this.maxSpeed = speed;
        this.maintenance = maintenance;
	this.annualFuelConsumption = annualFuelConsumption;
	this.fuelType = fuelType;
	this.waterCapacity = waterCapacity;
    }

    public int getFuelType() {
	return fuelType;
    }

    public int getAnnualFuelConsumption() {
	return annualFuelConsumption;
    }

    public int getWaterCapacity() {
	return waterCapacity;
    }
}
