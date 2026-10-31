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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TODO doesn't know what type it is (depot, station, terminal)? why not? should know.
/**
 * Represents a cargo-service station with a location, name, supply and demand
 * policy, and waiting cargo.
 */
public class Station extends Identifiable {

    @Serial
    private static final long serialVersionUID = 3256442503979874355L;
    private final Vec2D location;
    private final String name;
    private StationSupply supply;
    private StationDemand demandForCargo;
    private ImmutableCargoBatchBundle cargoBatchBundle;
    // Retains the old serialized field name so World can migrate legacy saves.
    private List<TrainTemplate> production;
    private transient List<TrainTemplate> legacyProduction;

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
    public Vec2D getLocation() {
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

    /**
     * Returns and clears the production queue read from a legacy station save.
     * The world uses this once to migrate old station queues into engine shops.
     *
     * @return legacy queued train templates, or an empty list
     */
    public List<TrainTemplate> takeLegacyProductionForMigration() {
        if (legacyProduction == null) {
            return List.of();
        }
        List<TrainTemplate> result = legacyProduction;
        legacyProduction = null;
        return result;
    }

    @Serial
    private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
        input.defaultReadObject();
        if (production != null && !production.isEmpty()) {
            legacyProduction = List.copyOf(production);
        }
        production = null;
    }

}