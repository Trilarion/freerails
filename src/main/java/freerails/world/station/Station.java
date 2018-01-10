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

package freerails.world.station;

import freerails.util.ImmutableList;

import java.io.Serializable;

/**
 * Represents a station. with a position, a name, a supply, a demand, a conversion (of cargo).
 * The content is effectively immutable.
 */
public class Station implements Serializable {

    private static final long serialVersionUID = 3256442503979874355L;
    // TODO position as Point2D
    public final int x;
    public final int y;
    private final String name;
    private final StationSupply supply;
    private final StationDemand demandForCargo;
    private final StationConversion cargoConversion;
    // TODO what is the cargo bundle number and what is it good for?
    private final int cargoBundleNumber;
    /**
     * What this station is building.
     */
    // TODO remove ImmutableList
    private final ImmutableList<TrainBlueprint> production;

    // TODO this may be a misuse, instead add cargoConversion, where is this used
    /**
     * Makes a copy of the station
     *
     * @param s
     * @param cargoConversion
     */
    public Station(Station s, StationConversion cargoConversion) {
        this.cargoConversion = cargoConversion;
        cargoBundleNumber = s.cargoBundleNumber;
        demandForCargo = s.demandForCargo;
        name = s.name;
        production = s.production;
        supply = s.supply;
        x = s.x;
        y = s.y;
    }

    /**
     * @param x
     * @param y
     * @param stationName
     * @param numberOfCargoTypes
     * @param cargoBundleNumber
     */
    public Station(int x, int y, String stationName,
                   int numberOfCargoTypes, int cargoBundleNumber) {
        name = stationName;
        this.x = x;
        this.y = y;
        this.cargoBundleNumber = cargoBundleNumber;

        // TODO array creation neccessary here?
        supply = new StationSupply(new int[numberOfCargoTypes]);
        production = new ImmutableList<>();
        demandForCargo = new StationDemand(new boolean[numberOfCargoTypes]);
        cargoConversion = StationConversion.emptyInstance(numberOfCargoTypes);

    }

    // TODO are these meaningful values
    /**
     *
     */
    public Station() {
        name = "No name";
        x = 0;
        y = 0;
        demandForCargo = new StationDemand(new boolean[0]);
        supply = new StationSupply(new int[0]);
        cargoConversion = new StationConversion(new Integer[0]);
        production = new ImmutableList<>();
        cargoBundleNumber = 0;
    }

    // TODO this might be a misuse, just add production as method instead, copy should not be neccessary
    /**
     * @param s
     * @param production
     */
    public Station(Station s, ImmutableList<TrainBlueprint> production) {
        this.production = production;
        demandForCargo = s.demandForCargo;
        cargoBundleNumber = s.cargoBundleNumber;
        cargoConversion = s.cargoConversion;
        name = s.name;
        supply = s.supply;
        x = s.x;
        y = s.y;
    }

    // TODO possible misuse, see above
    /**
     * @param s
     * @param demandForCargo
     */
    public Station(Station s, StationDemand demandForCargo) {
        this.demandForCargo = demandForCargo;
        cargoBundleNumber = s.cargoBundleNumber;
        cargoConversion = s.cargoConversion;
        name = s.name;
        production = s.production;
        supply = s.supply;
        x = s.x;
        y = s.y;
    }

    // TODO possible misuse, see above
    /**
     * @param s
     * @param supply
     */
    public Station(Station s, StationSupply supply) {
        this.supply = supply;
        demandForCargo = s.demandForCargo;

        cargoBundleNumber = s.cargoBundleNumber;
        cargoConversion = s.cargoConversion;
        name = s.name;
        production = s.production;
        x = s.x;
        y = s.y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Station))
            return false;
        final Station station = (Station) o;
        if (cargoBundleNumber != station.cargoBundleNumber)
            return false;
        if (x != station.x)
            return false;
        if (y != station.y)
            return false;
        if (cargoConversion != null ? !cargoConversion.equals(station.cargoConversion)
                : station.cargoConversion != null)
            return false;
        if (demandForCargo != null ? !demandForCargo.equals(station.demandForCargo)
                : station.demandForCargo != null)
            return false;
        if (!name.equals(station.name))
            return false;
        if (production != null ? !production.equals(station.production)
                : station.production != null)
            return false;
        return supply != null ? supply.equals(station.supply) : station.supply == null;
    }

    @Override
    public int hashCode() {
        int result;
        result = x;
        result = 29 * result + y;
        result = 29 * result + (name != null ? name.hashCode() : 0);
        result = 29 * result + (supply != null ? supply.hashCode() : 0);
        result = 29 * result + (demandForCargo != null ? demandForCargo.hashCode() : 0);
        result = 29 * result + (cargoConversion != null ? cargoConversion.hashCode() : 0);
        result = 29 * result + cargoBundleNumber;
        result = 29 * result + production.size();
        return result;
    }

    /**
     * @return
     */
    public StationConversion getCargoConversion() {
        return cargoConversion;
    }

    /**
     * @return
     */
    public String getStationName() {
        return name;
    }

    /**
     * @return
     */
    public int getStationX() {
        return x;
    }

    /**
     * @return
     */
    public int getStationY() {
        return y;
    }

    /**
     * @return
     */
    public ImmutableList<TrainBlueprint> getProduction() {
        return production;
    }

    /**
     * @return
     */
    public StationDemand getDemandForCargo() {
        return demandForCargo;
    }

    /**
     * @return
     */
    public StationSupply getSupply() {
        return supply;
    }

    /**
     * @return
     */
    public int getCargoBundleID() {
        return cargoBundleNumber;
    }

}