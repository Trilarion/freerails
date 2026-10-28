/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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

import freerails.util.Identifiable;
import freerails.model.cargo.CargoBatchBundle;
import freerails.model.cargo.UnmodifiableCargoBatchBundle;

import freerails.model.train.TrainTemplate;
import freerails.util.Vec2D;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// TODO doesn't know what type it is (depot, station, terminal)? why not? should know.
/**
 * Represents a station with a location, name, cargo service data, and
 * production queue. Its cargo bundle is copied on construction and replacement.
 */
public class Station extends Identifiable {

    @Serial
    private static final long serialVersionUID = 3256442503979874355L;
    private final Vec2D location;
    private final String name;
    private StationSupply supply;
    private StationDemand demandForCargo;
    private StationCargoConversion cargoConversion;
    private CargoBatchBundle cargoBatchBundle;

    private List<TrainTemplate> production;

    /**
     * Creates a station and initializes its cargo settings for the world's
     * cargo types.
     *
     * @param id station identifier
     * @param location map location of the station
     * @param stationName display name
     * @param numberOfCargoTypes number of cargo types in the world
     * @param cargoBatchBundle initial waiting cargo, copied by this station
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
     * Returns the station's cargo conversion rules.
     *
     * @return cargo conversion settings
     */
    public StationCargoConversion getCargoConversion() {
        return cargoConversion;
    }

    /**
     * Returns the station's display name.
     *
     * @return station name
     */
    public String getStationName() {
        return name;
    }

    /**
     * Returns the station's map coordinates.
     *
     * @return station location
     */
    public Vec2D getLocation() {
        return location;
    }

    /**
     * Returns the train templates currently in the station's production queue.
     *
     * @return production queue
     */
    public List<TrainTemplate> getProduction() {
        return production;
    }

    /**
     * Returns the station's cargo demand settings.
     *
     * @return cargo demand
     */
    public StationDemand getDemandForCargo() {
        return demandForCargo;
    }

    /**
     * Returns the station's cargo supply rates.
     *
     * @return cargo supply
     */
    public StationSupply getSupply() {
        return supply;
    }

    /**
     * Returns the cargo currently waiting at the station through the
     * unmodifiable bundle interface.
     *
     * @return waiting cargo
     */
    public UnmodifiableCargoBatchBundle getCargoBatchBundle() {
        return cargoBatchBundle;
    }

    /**
     * Replaces the waiting cargo with a copy of the supplied bundle.
     *
     * @param cargoBatchBundle new waiting cargo to copy
     */
    public void setCargoBatchBundle(@NotNull UnmodifiableCargoBatchBundle cargoBatchBundle) {
        this.cargoBatchBundle = new CargoBatchBundle(cargoBatchBundle);
    }

    /**
     * Replaces the station's cargo conversion rules.
     *
     * @param cargoConversion conversion rules to install
     */
    public void setCargoConversion(StationCargoConversion cargoConversion) {
        this.cargoConversion = cargoConversion;
    }

    /**
     * Replaces the station's cargo demand settings.
     *
     * @param demandForCargo demand settings to install
     */
    public void setDemandForCargo(StationDemand demandForCargo) {
        this.demandForCargo = demandForCargo;
    }

    /**
     * Replaces the station's cargo supply rates.
     *
     * @param supply supply rates to install
     */
    public void setSupply(StationSupply supply) {
        this.supply = supply;
    }

    /**
     * Replaces the train templates in the station's production queue.
     *
     * @param production ordered production queue
     */
    public void setProduction(List<TrainTemplate> production) {
        this.production = production;
    }
}