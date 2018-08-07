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

import freerails.model.Identifiable;
import freerails.model.track.TrackPiece;
import freerails.model.track.TrackType;

import freerails.util.Vec2D;
import freerails.model.world.PlayerKey;
import freerails.model.player.Player;
import freerails.model.terrain.TerrainTile;
import freerails.model.world.UnmodifiableWorld;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// TODO doesn't know if depot, station, terminal? why not?
/**
 * Represents a station. with a position, a name, a supply, a demand, a conversion (of cargo).
 * The content is effectively immutable.
 */
public class Station extends Identifiable {

    private static final long serialVersionUID = 3256442503979874355L;
    public final Vec2D location;
    private final String name;
    private StationSupply supply;
    private StationDemand demandForCargo;
    private StationCargoConversion cargoConversion;
    // TODO what is the cargo bundle number and what is it good for?
    private final int cargoBundleNumber;

    private List<TrainBlueprint> production;

    /**
     * @param location
     * @param stationName
     * @param numberOfCargoTypes
     * @param cargoBundleNumber
     */
    public Station(int id, Vec2D location, String stationName, int numberOfCargoTypes, int cargoBundleNumber) {
        super(id);
        name = stationName;
        this.location = location;
        this.cargoBundleNumber = cargoBundleNumber;

        // TODO array creation necessary here?
        Integer[] a = new Integer[numberOfCargoTypes];
        Arrays.fill(a, 0);
        supply = new StationSupply(a);
        production = new ArrayList<>();
        demandForCargo = new StationDemand(new boolean[numberOfCargoTypes]);
        cargoConversion = StationCargoConversion.emptyInstance(numberOfCargoTypes);
    }

    // TODO static code somewhere else?
    /**
     * Return Station number if station exists at location or -1
     */
    public static int getStationIdAtLocation(UnmodifiableWorld world, Player player, Vec2D location) {
        TerrainTile tile = (TerrainTile) world.getTile(location);
        TrackPiece trackPiece = tile.getTrackPiece();

        if (trackPiece != null) {
            TrackType trackType = trackPiece.getTrackType();
            if (trackType.isStation() && trackPiece.getOwnerID() == world.getID(player)) {

                for (Station station: world.getStations(player)) {
                    if (location.equals(station.location)) {
                        return station.getId();
                    }
                }
            }
        }

        return -1;
        // Don't show terrain...
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
    public List<TrainBlueprint> getProduction() {
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

    public void setProduction(List<TrainBlueprint> production) {
        this.production = production;
    }
}