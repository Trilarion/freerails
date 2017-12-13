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

package org.railz.world.station;

import java.util.*;

import org.railz.world.common.*;
/**
 * This class represents a station.
 *
 * @author Luke
 *
 */
public class StationModel implements FreerailsSerializable {
    static final long serialVersionUID = 5866243529680687772L;

    public final int x;
    public final int y;
    private final String name;
    private SupplyAtStation supply;
    private DemandAtStation demand;
    private ConvertedAtStation converted;
    private final int cargoBundleNumber;
    private final GameTime creationDate;
    private int[] improvements;

    /** What this station is building. */
    private ProductionAtEngineShop production;

    public ConvertedAtStation getConverted() {
        return converted;
    }

    public StationModel(StationModel s, ConvertedAtStation converted) {
	this(s);
        this.converted = converted;
    }

    public StationModel(int x, int y, String stationName,
        int numberOfCargoTypes, int cargoBundle, GameTime now) {
        this.name = stationName;
        this.x = x;
        this.y = y;
        production = null;
	creationDate = now;

        supply = new SupplyAtStation(new int[numberOfCargoTypes]);
        demand = new DemandAtStation(new boolean[numberOfCargoTypes]);
        converted = ConvertedAtStation.emptyInstance(numberOfCargoTypes);
        cargoBundleNumber = cargoBundle;
	improvements = new int[0];
    }

    public String getStationName() {
        return name;
    }

    public int getStationX() {
        return x;
    }

    public int getStationY() {
        return y;
    }

    public ProductionAtEngineShop getProduction() {
        return production;
    }

    public StationModel(StationModel s, ProductionAtEngineShop production) {
	this(s);
        this.production = production;
    }

    public DemandAtStation getDemand() {
        return demand;
    }

    public SupplyAtStation getSupply() {
        return supply;
    }

    public StationModel(StationModel s, DemandAtStation demand) {
	this(s);
        this.demand = demand;
    }

    public StationModel(StationModel s) {
	x = s.x;
	y = s.y;
	name = s.name;
	supply = s.supply;
	demand = s.demand;
	converted = s.converted;
	cargoBundleNumber = s.cargoBundleNumber;
	creationDate = s.creationDate;
	improvements = (int[]) s.improvements.clone();
	production = s.production;
    }

    public StationModel(StationModel s, SupplyAtStation supply) {
	this(s);
        this.supply = supply;
    }

    public int getCargoBundleNumber() {
        return cargoBundleNumber;
    }

    public boolean equals(Object o) {
        if (o instanceof StationModel) {
            StationModel test = (StationModel)o;

	    if (cargoBundleNumber == test.cargoBundleNumber &&
		    x == test.x &&
		    y == test.y &&
		    demand.equals(test.demand) &&
		    converted.equals(test.converted) &&
		    name.equals(test.name) &&
		    (production == null ? test.production == null :
		     this.production.equals(test.production)) &&
		    supply.equals(test.supply) &&
		    creationDate.equals(test.creationDate) && 
		    Arrays.equals(improvements, test.improvements)) {
                return true;
            }
            return false;
        } else {
            return false;
        }
    }

    /**
     * @return the date at which this station was constructed.
     */
    public GameTime getCreationDate() {
	return creationDate;
    }

    /** @return A clone of indices into the STATION_IMPROVEMENTS table */
    public int[] getImprovements() {
	return (int[]) improvements.clone();
    }

    public boolean hasImprovement(int improvementId) {
	for (int i = 0; i < improvements.length; i++)
	    if (improvements[i] == improvementId)
		return true;
	return false;
    }

    /** @param improvements Indices into the STATION_IMPROVEMENTS table */
    public StationModel setImprovements(int[] improvements) {
	StationModel sm = new StationModel(this);
	sm.improvements = (int[]) improvements.clone();
	return sm;
    }

    public String toString() {
	String s = "StationModel: x=" + x + ", y=" + y + ", name=" + name +
	    ", supply=" + supply + ", demand=" + demand + ", converted=" +
	    converted + ", cargoBundleNumber=" + cargoBundleNumber +
	    ", creationDate=" + creationDate + ", improvements=(";
	for (int i = 0; i < improvements.length; i++)
	    s += improvements[i] + ", ";

	s += ")";
	return s;
    }
}
