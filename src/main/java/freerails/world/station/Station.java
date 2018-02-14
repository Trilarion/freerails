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
import freerails.util.Vector2D;
import freerails.world.KEY;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.terrain.FullTerrainTile;
import freerails.world.track.TrackRule;
import freerails.world.world.ReadOnlyWorld;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Represents a station. with a position, a name, a supply, a demand, a conversion (of cargo).
 * The content is effectively immutable.
 */
public class Station implements Serializable {

    private static final long serialVersionUID = 3256442503979874355L;
    public final Vector2D location;
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
     */
    public Station(Station s, StationConversion cargoConversion) {
        this.cargoConversion = cargoConversion;
        cargoBundleNumber = s.cargoBundleNumber;
        demandForCargo = s.demandForCargo;
        name = s.name;
        production = s.production;
        supply = s.supply;
        location = s.location;
    }

    /**
     * @param location
     * @param stationName
     * @param numberOfCargoTypes
     * @param cargoBundleNumber
     */
    public Station(Vector2D location, String stationName, int numberOfCargoTypes, int cargoBundleNumber) {
        name = stationName;
        this.location = location;
        this.cargoBundleNumber = cargoBundleNumber;

        // TODO array creation necessary here?
        Integer[] a = new Integer[numberOfCargoTypes];
        Arrays.fill(a, 0);
        supply = new StationSupply(a);
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
        location = Vector2D.ZERO;
        demandForCargo = new StationDemand(new boolean[0]);
        supply = new StationSupply(new Integer[0]);
        cargoConversion = new StationConversion(new Integer[0]);
        production = new ImmutableList<>();
        cargoBundleNumber = 0;
    }

    // TODO this might be a misuse, just add production as method instead, copy should not be necessary
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
        location = s.location;
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
        location = s.location;
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
        location = s.location;
    }

    /**
     * Return Station number if station exists at location or -1
     */
    public static int getStationNumberAtLocation(ReadOnlyWorld world, FreerailsPrincipal principal, Vector2D p) {
        FullTerrainTile tile = (FullTerrainTile) world.getTile(p);

        TrackRule trackRule = tile.getTrackPiece().getTrackRule();
        if (trackRule.isStation() && tile.getTrackPiece().getOwnerID() == world.getID(principal)) {

            for (int i = 0; i < world.size(principal, KEY.STATIONS); i++) {
                Station station = (Station) world.get(principal, KEY.STATIONS, i);

                if (null != station && p.equals(station.location)) {
                    return i;
                }
            }
        }
        return -1;
        // Don't show terrain...
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Station)) return false;
        final Station station = (Station) obj;
        if (cargoBundleNumber != station.cargoBundleNumber) return false;
        if (!location.equals(station.location)) return false;
        if (cargoConversion != null ? !cargoConversion.equals(station.cargoConversion) : station.cargoConversion != null)
            return false;
        if (demandForCargo != null ? !demandForCargo.equals(station.demandForCargo) : station.demandForCargo != null)
            return false;
        if (!name.equals(station.name)) return false;
        if (production != null ? !production.equals(station.production) : station.production != null) return false;
        return supply != null ? supply.equals(station.supply) : station.supply == null;
    }

    @Override
    public int hashCode() {
        int result;
        result = location.hashCode();
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
    public Vector2D getStationP() {
        return location;
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