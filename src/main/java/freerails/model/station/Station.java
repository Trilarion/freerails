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
import freerails.model.cargo.CargoBatchBundle;
import freerails.model.cargo.UnmodifiableCargoBatchBundle;
import freerails.model.track.TrackPiece;
import freerails.model.track.TrackType;

import freerails.model.train.TrainTemplate;
import freerails.util.Vec2D;
import freerails.model.player.Player;
import freerails.model.terrain.TerrainTile;
import freerails.model.world.UnmodifiableWorld;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// TODO doesn't know what type it is (depot, station, terminal)? why not? should know.
/**
 * Represents a station. with a position, a name, a supply, a demand, a conversion (of cargo).
 * The content is effectively immutable.
 */
public class Station extends Identifiable {

    private static final long serialVersionUID = 3256442503979874355L;
    private final Vec2D location;
    private final String name;
    private StationSupply supply;
    private StationDemand demandForCargo;
    private StationCargoConversion cargoConversion;
    private CargoBatchBundle cargoBatchBundle;

    private List<TrainTemplate> production;

    /**
     * @param location
     * @param stationName
     * @param numberOfCargoTypes
     * @param cargoBatchBundle
     */
    public Station(int id, Vec2D location, String stationName, int numberOfCargoTypes, @NotNull UnmodifiableCargoBatchBundle cargoBatchBundle) {
        super(id);
        name = stationName;
        this.location = location;
        this.cargoBatchBundle = new CargoBatchBundle(cargoBatchBundle);

        // TODO array creation necessary here?
        Integer[] a = new Integer[numberOfCargoTypes];
        Arrays.fill(a, 0);
        supply = new StationSupply(a);
        production = new ArrayList<>();
        demandForCargo = new StationDemand(new boolean[numberOfCargoTypes]);
        cargoConversion = StationCargoConversion.emptyInstance(numberOfCargoTypes);
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
    public List<TrainTemplate> getProduction() {
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
    public UnmodifiableCargoBatchBundle getCargoBatchBundle() {
        return cargoBatchBundle;
    }

    /**
     * Makes a copy.
     *
     * @param cargoBatchBundle
     */
    public void setCargoBatchBundle(@NotNull UnmodifiableCargoBatchBundle cargoBatchBundle) {
        this.cargoBatchBundle = new CargoBatchBundle(cargoBatchBundle);
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

    public void setProduction(List<TrainTemplate> production) {
        this.production = production;
    }
}