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

package freerails.model.station;

import freerails.model.track.TrackPiece;
import freerails.model.track.TrackRule;
import freerails.util.ImmutableList;
import freerails.util.Vec2D;
import freerails.model.world.PlayerKey;
import freerails.model.player.FreerailsPrincipal;
import freerails.model.terrain.TerrainTile;
import freerails.model.world.UnmodifiableWorld;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

// TODO doesn't know if depot, station, terminal? why not?
/**
 * Represents a station. with a position, a name, a supply, a demand, a conversion (of cargo).
 * The content is effectively immutable.
 */
public class Station implements Serializable {

    private static final long serialVersionUID = 3256442503979874355L;
    public final Vec2D location;
    private final String name;
    private StationSupply supply;
    private StationDemand demandForCargo;
    private StationCargoConversion cargoConversion;
    // TODO what is the cargo bundle number and what is it good for?
    private final int cargoBundleNumber;

    private ImmutableList<TrainBlueprint> production;

    /**
     * @param location
     * @param stationName
     * @param numberOfCargoTypes
     * @param cargoBundleNumber
     */
    public Station(Vec2D location, String stationName, int numberOfCargoTypes, int cargoBundleNumber) {
        name = stationName;
        this.location = location;
        this.cargoBundleNumber = cargoBundleNumber;

        // TODO array creation necessary here?
        Integer[] a = new Integer[numberOfCargoTypes];
        Arrays.fill(a, 0);
        supply = new StationSupply(a);
        production = new ImmutableList<>();
        demandForCargo = new StationDemand(new boolean[numberOfCargoTypes]);
        cargoConversion = StationCargoConversion.emptyInstance(numberOfCargoTypes);
    }

    /**
     * Return Station number if station exists at location or -1
     */
    public static int getStationNumberAtLocation(UnmodifiableWorld world, FreerailsPrincipal principal, Vec2D location) {
        TerrainTile tile = (TerrainTile) world.getTile(location);
        TrackPiece trackPiece = tile.getTrackPiece();

        if (trackPiece != null) {
            TrackRule trackRule = trackPiece.getTrackRule();
            if (trackRule.isStation() && trackPiece.getOwnerID() == world.getID(principal)) {

                for (int i = 0; i < world.size(principal, PlayerKey.Stations); i++) {
                    Station station = (Station) world.get(principal, PlayerKey.Stations, i);

                    if (null != station && location.equals(station.location)) {
                        return i;
                    }
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
    public StationCargoConversion getCargoConversion() {
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
    public Vec2D getLocation() {
        return location;
    }

    /**
     * What this station is building.
     */ /**
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

    public void setCargoConversion(StationCargoConversion cargoConversion) {
        this.cargoConversion = cargoConversion;
    }

    public void setDemandForCargo(StationDemand demandForCargo) {
        this.demandForCargo = demandForCargo;
    }

    public void setSupply(StationSupply supply) {
        this.supply = supply;
    }

    public void setProduction(ImmutableList<TrainBlueprint> production) {
        this.production = production;
    }
}