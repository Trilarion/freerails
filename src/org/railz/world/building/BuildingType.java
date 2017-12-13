/*
 * Copyright (C) 2004 Robert Tuck
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
package org.railz.world.building;

import java.awt.Point;

import org.railz.world.common.*;
import org.railz.world.top.*;
/**
 * Defines a type of building.
 * @author rtuck99@users.berlios.de
 */
public class BuildingType implements FreerailsSerializable {
    static final long serialVersionUID = -3750704386608641745L;

    private final boolean[] validTrackLayouts = new boolean[256];
    private final Production[] production;
    private final Consumption[] consumption;
    private final Conversion[] conversion;
    private final long baseValue;
    private final String name;
    private final int stationRadius;
    private final int category;
    private final boolean[] acceptableTerrainTypes;
    private final boolean[] neighbouringTerrainTypes;
    private transient final DistributionParams distributionParams[];

    public static final int CATEGORY_INDUSTRY = 0;
    public static final int CATEGORY_RESOURCE = 1;
    public static final int CATEGORY_URBAN = 2;
    public static final int CATEGORY_STATION = 3;

    /**
     * See comments in DTD for meanings of a, b, c.
     * XXX This kind of info should not be part of the world object but for
     * now leave it in this class since we have nowhere else to put it.
     */
    public static class DistributionParams {
	public double a;
	public double b;
	public double c;

	public DistributionParams(double a, double b, double
		c) {
	    this.a = a;
	    this.b = b;
	    this.c = c;
	}
    }

    /**
     * @return true if the terrain types on the squares near this tile are OK
     * to build on.
     * @param p position at which to build
     */
    public boolean canBuildHere(World w, Point p) {
	int terrainType = w.getTile(p).getTerrainTypeNumber();
	if (! acceptableTerrainTypes[terrainType])
	    return false;

	int xmin = (p.x == 0) ? 0 : p.x - 1;
	int ymin = (p.y == 0) ? 0 : p.y - 1;
	int xmax = (p.x == w.getMapWidth()) ? w.getMapWidth() : p.x + 1;
	int ymax = (p.y == w.getMapHeight()) ? w.getMapHeight() : p.y + 1;
	for (int x = xmin; x <= xmax; x++) {
	    for (int y = ymin; y <= ymax; y++) {
		if (x == p.x && y == p.y)
		    continue;
		terrainType = w.getTile(x, y).getTerrainTypeNumber();
		if (neighbouringTerrainTypes[terrainType])
		    return true;
	    }
	}
	return false;
    }

    private void setTrackLayouts(byte[] trackLayouts) {
	for (int i = 0; i < trackLayouts.length; i++) {
	    int layout = trackLayouts[i];
	    for (int j = 0; j < 8; j++) {
		validTrackLayouts[layout] = true;
		layout = CompassPoints.rotateClockwise((byte) layout) & 0xFF;
	    }
	}
    }

    public BuildingType(String name, long baseValue, int stationRadius, byte[]
	    validTrackLayouts, boolean[] acceptableTerrainTypes, boolean[]
	    neighbouringTerrainTypes, DistributionParams[] distributionParams) {
	this.name = name;
	this.baseValue = baseValue;
	this.stationRadius = stationRadius;
	production = new Production[0];
	consumption = new Consumption[0];
	conversion = new Conversion[0];
	category = CATEGORY_STATION;
	setTrackLayouts(validTrackLayouts);
	this.neighbouringTerrainTypes = neighbouringTerrainTypes;
	this.acceptableTerrainTypes = acceptableTerrainTypes;
	this.distributionParams = distributionParams;
    }

    public BuildingType(String name, Production[] production, Consumption[]
	    consumption, Conversion[] conversion, long baseValue, int
	    category, byte[] validTrackLayouts, boolean[]
	    acceptableTerrainTypes,  boolean[]
	    neighbouringTerrainTypes, DistributionParams[] distributionParams) {
	this.name = name;
	this.production = production;
	this.consumption = consumption;
	this.conversion = conversion;
	this.baseValue = baseValue;
	this.category = category;
	stationRadius = 0;
	setTrackLayouts(validTrackLayouts);
	this.neighbouringTerrainTypes = neighbouringTerrainTypes;
	this.acceptableTerrainTypes = acceptableTerrainTypes;
	this.distributionParams = distributionParams;
    }

    public Production[] getProduction() {
	return production;
    }

    public Consumption[] getConsumption() {
	return consumption;
    }

    public Conversion[] getConversion() {
	return conversion;
    }

    public long getBaseValue() {
	return baseValue;
    }

    public String getName() {
	return name;
    }

    public int getCategory() {
	return category;
    }

    public int getStationRadius() {
	return stationRadius;
    }

    public boolean isTrackLayoutValid(byte trackLayout) {
	return validTrackLayouts[trackLayout & 0xFF];
    }

    /**
     * Calculate the probability of this building type appearing at a given
     * map position
     */
    public double distributionProbability(ReadOnlyWorld w, Point p) {
	int tt = w.getTile(p).getTerrainTypeNumber();
	return distributionParams[tt].c;
    }

    /**
     * Calculate the probability of this building type appearing at a given
     * point within a city
     */
    public double distributionProbability(ReadOnlyWorld w, Point p, Point
	    cityCentre) {
	int tt = w.getTile(p).getTerrainTypeNumber();
	int dx = p.x - cityCentre.x;
	int dy = p.y - cityCentre.y;
	double r = Math.sqrt(dx * dx + dy * dy);
	return distributionParams[tt].a * Math.exp(-r *
		distributionParams[tt].b);
    }
}
