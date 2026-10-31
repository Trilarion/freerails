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
import freerails.model.cargo.ImmutableCargoBatchBundle;
import freerails.model.cargo.UnmodifiableCargoBatchBundle;

import freerails.model.train.TrainTemplate;
import freerails.util.Vec2D;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TODO doesn't know what type it is (depot, station, terminal)? why not? should know.
/**
 * Represents a station with a location, name, cargo service data, and
 * production queue. Cargo service data is keyed by stable cargo IDs, and its
 * cargo bundle is stored as an immutable snapshot.
 */
public class Station extends Identifiable {

    @Serial
    private static final long serialVersionUID = 3256442503979874355L;
    private final Vec2D location;
    private final String name;
    private StationSupply supply;
    private StationDemand demandForCargo;
    private StationCargoConversion cargoConversion;
    private ImmutableCargoBatchBundle cargoBatchBundle;

    private final List<TrainTemplate> production;

    /**
     * Creates a station with empty cargo demand and supply settings.
     *
     * @param id station identifier
     * @param location map location of the station
     * @param stationName display name
     * @param cargoBatchBundle initial waiting cargo, copied by this station
     */
    public Station(int id, Vec2D location, String stationName, @NotNull UnmodifiableCargoBatchBundle cargoBatchBundle) {
        super(id);
        name = stationName;
        this.location = location;
        this.cargoBatchBundle = new ImmutableCargoBatchBundle(cargoBatchBundle);

        supply = new StationSupply(Map.of());
        production = new ArrayList<>();
        demandForCargo = new StationDemand(Set.of());
        cargoConversion = new StationCargoConversion(Map.of());
    }

    /**
     * Creates a copy of a station, including an immutable snapshot of its cargo.
     *
     * @param station station to copy
     */
    public Station(@NotNull Station station) {
        super(station.getId());
        location = station.location;
        name = station.name;
        supply = station.supply;
        demandForCargo = station.demandForCargo;
        cargoConversion = station.cargoConversion;
        cargoBatchBundle = new ImmutableCargoBatchBundle(station.cargoBatchBundle);
        production = new ArrayList<>(station.production);
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
     * @return an unmodifiable view of the production queue
     */
    public List<TrainTemplate> getProduction() {
        return Collections.unmodifiableList(production);
    }

    /**
     * Adds a train template to the end of the production queue.
     *
     * @param trainTemplate template to queue
     */
    public void queueProduction(@NotNull TrainTemplate trainTemplate) {
        production.add(trainTemplate);
    }

    /**
     * Removes the first matching train template from the production queue.
     *
     * @param trainTemplate template to remove
     * @return whether a matching template was removed
     */
    public boolean removeProduction(@NotNull TrainTemplate trainTemplate) {
        return production.remove(trainTemplate);
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
        this.cargoBatchBundle = new ImmutableCargoBatchBundle(cargoBatchBundle);
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
        List<TrainTemplate> productionCopy = List.copyOf(production);
        this.production.clear();
        this.production.addAll(productionCopy);
    }
}