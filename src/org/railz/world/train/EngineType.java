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

import org.railz.world.common.*;
import org.railz.world.track.*;

/**
 * This class represents an engine type, for example 'Grass Hopper'.  It
 * encapsulates the properties that are common to all engines of the same
 * type.
 *
 * @author Luke
 */
final public class EngineType implements FreerailsSerializable {
    //static final long serialVersionUID = -4063387491848236675L;

    public static final int FUEL_TYPE_COAL = 1;
    public static final int FUEL_TYPE_DIESEL = 2;
    public static final int FUEL_TYPE_ELECTRIC = 3;

    /**
     * This is the conversion factor between "real" units and game-world ones
     */
    public static final float TILE_HOURS_PER_MILE_BIGTICKS = 1.0f / 25;

    private final String engineTypeName;
    private final int fuelType;
    private final long price;
    private final long maintenance;
    private final int waterCapacity; // waterCapacity in units
    private final int mass; // mass in tonnes

    /** fuel consumption in units per year */
    private final int annualFuelConsumption;
    private final float powerOutput;
    private final float maxTractiveForce;
    private final float dragCoefficient;
    private final float rollingFrictionCoefficient;
    private boolean available;

    /**
     * @return annual maintenance expense
     */
    public long getMaintenance() {
        return maintenance;
    }

    /** @return a unique resource key to the name of this engine */
    public String getEngineTypeName() {
        return engineTypeName;
    }

    public long getPrice() {
        return price;
    }

    public EngineType setAvailable(boolean b) {
	EngineType et = new EngineType(this);
	et.available = b;
	return et;
    }

    public EngineType(EngineType et) {
	engineTypeName = et.engineTypeName;
	fuelType = et.fuelType;
	price = et.price;
	maintenance = et.maintenance;
	waterCapacity = et.waterCapacity;
	mass = et.mass;
	annualFuelConsumption = et.annualFuelConsumption;
	powerOutput = et.powerOutput;
	maxTractiveForce = et.maxTractiveForce;
	dragCoefficient = et.dragCoefficient;
	rollingFrictionCoefficient = et.rollingFrictionCoefficient;
	available = et.available;
    }

    public EngineType(String name, long m,
        long maintenance, int annualFuelConsumption, int fuelType,
	int waterCapacity, int mass, int powerOutput, int maxTractiveForce,
	float rollingFrictionCoefficient, float dragCoefficient, boolean
	available) {
        engineTypeName = name;
        price = m;
        this.maintenance = maintenance;
	this.annualFuelConsumption = annualFuelConsumption;
	this.fuelType = fuelType;
	this.waterCapacity = waterCapacity;
	this.mass = mass;
	this.powerOutput = (float) powerOutput / 100;
	this.maxTractiveForce = (float) maxTractiveForce / 10;
	this.dragCoefficient = dragCoefficient;
	this.rollingFrictionCoefficient = rollingFrictionCoefficient;
	this.available = available;
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

    public int getMass() {
	return mass;
    }

    public float getPowerOutput() {
	return powerOutput;
    }

    public float getMaxTractiveForce() {
	return maxTractiveForce;
    }

    public float getDragCoeff() {
	return dragCoefficient;
    }

    public float getRollingFrictionCoeff() {
	return rollingFrictionCoefficient;
    }

    /**
     * @param effectiveIncline effectiveIncline in %
     * @return acceleration at the given speed and incline */
    public float getAcceleration(float effectiveIncline, float v, int
	    totalMass, boolean outOfWater) {
	float tractiveForce = powerOutput / (outOfWater ? 3 * v : v);
	// at low speeds, acceleration is limited by traction
	if (tractiveForce > (float) maxTractiveForce)
	    tractiveForce = (float) maxTractiveForce;
	// calculate acceleration
	// since mass is on the order of 100, and v is of the order of 1, and
	// mass is of the order of 1, then power should be in the region of
	// 100
	return tractiveForce / (float) totalMass -
	    effectiveIncline * 0.098f - (v * v) * dragCoefficient / 
	    ((float) totalMass) - v * rollingFrictionCoefficient;
    }

    /**
     * @param mass of train + engine in tonnes
     * @param effectiveIncline effective incline in %
     * @return maximum speed in deltas per tick
     */
    public float getMaxSpeed(float effectiveIncline, int mass) {
	float increment = ((float) (5 *
		    EngineType.TILE_HOURS_PER_MILE_BIGTICKS *
		    TrackTile.DELTAS_PER_TILE)) / GameTime.TICKS_PER_BIG_TICK;
	float vMin = 0;
	float vMax = 0;
	float aMin, aMax;
	do {
	    vMin = vMax;
	    vMax += increment;
	    aMin = getAcceleration(effectiveIncline, vMin, mass, false);
	    aMax = getAcceleration(effectiveIncline, vMax, mass, false);
	} while (aMax > 0.0f);
	if (aMax == 0.0f)
	    return vMax;

	float newV, newA;
	// estimate by bisection
	do {
	    newV = (vMin + vMax) / 2;
	    newA = getAcceleration(effectiveIncline, newV, mass, false);
	    if (newA == 0.0f)
		return newV;
	    if (newA > 0.0f) { 
		vMin = newV;
		aMin = newA;
	    } else {
		vMax = newV;
		aMax = newA;
	    }
	} while (vMax - vMin > 0.0002f);
	return (vMax + vMin) / 2;
    }

    /** @return whether this train is available for purchase */
    public boolean isAvailable() {
	return available;
    }

    public boolean equals(Object o) {
	if (!(o instanceof EngineType))
	    return false;
	EngineType et = (EngineType) o;
	return engineTypeName.equals(et.engineTypeName) &&
	    et.available == available;
    }

    public int hashCode() {
	return engineTypeName.hashCode();
    }

    public String toString() {
	return "EngineType: name=" + engineTypeName + ", available=" +
	    available;
    }
}
