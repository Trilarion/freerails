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

package freerails.model.operations.station;

import freerails.util.Identifiable;
import freerails.model.operations.cargo.ImmutableCargoBatchBundle;
import freerails.model.operations.cargo.UnmodifiableCargoBatchBundle;

import freerails.util.IntPoint2D;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.util.Map;
import java.util.Set;

/**
 * Represents a cargo-service station with a location, name, supply and demand
 * policy, and waiting cargo. Its type (depot, station, or terminal) is the
 * track type at its location, available from the world tile.
 */
public class Station extends Identifiable {

    @Serial
    private static final long serialVersionUID = 1L;
    private final IntPoint2D location;
    private final String name;
    private StationSupply supply;
    private StationDemand demandForCargo;
    private ImmutableCargoBatchBundle cargoBatchBundle;

    /**
     * Creates a station with empty cargo demand and supply settings.
     *
     * @param id station identifier
     * @param location map location of the station
     * @param stationName display name
     * @param cargoBatchBundle initial waiting cargo, copied by this station
     */
    public Station(int id, IntPoint2D location, String stationName, @NotNull UnmodifiableCargoBatchBundle cargoBatchBundle) {
        super(id);
        name = stationName;
        this.location = location;
        this.cargoBatchBundle = new ImmutableCargoBatchBundle(cargoBatchBundle);

        supply = new StationSupply(Map.of());
        demandForCargo = new StationDemand(Set.of());
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
        cargoBatchBundle = new ImmutableCargoBatchBundle(station.cargoBatchBundle);
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
    public IntPoint2D getLocation() {
        return location;
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

}