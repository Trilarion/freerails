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

package freerails.move;

import freerails.model.player.Player;
import freerails.model.operations.station.Station;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.world.World;
import freerails.util.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Adds a station owned by the specified player to the world.
 */
public class AddStationMove implements Move {

    private final Player player;
    private final Station station;

    /**
     * Creates an add station move from the supplied values.
     *
     * @param player the player
     * @param station the station
     */
    public AddStationMove(@NotNull Player player, @NotNull Station station) {
        this.player = player;
        this.station = new Station(station);
    }

    /**
     * Returns whether this object is equal to the specified object.
     *
     * @param o the object to compare with this object
     * @return true if this object is equal to the specified object, otherwise false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AddStationMove other)) return false;

        return player.equals(other.player)
                && station.getId() == other.station.getId()
                && Objects.equals(station.getLocation(), other.station.getLocation())
                && Objects.equals(station.getStationName(), other.station.getStationName())
                && Objects.equals(station.getSupply(), other.station.getSupply())
                && Objects.equals(station.getDemandForCargo(), other.station.getDemandForCargo())
                && Objects.equals(station.getCargoBatchBundle(), other.station.getCargoBatchBundle());
    }

    /**
     * Returns a hash code for this object.
     *
     * @return the hash code for this object
     */
    @Override
    public int hashCode() {
        return Objects.hash(player, station.getId(), station.getLocation(), station.getStationName(),
                station.getSupply(), station.getDemandForCargo(), station.getCargoBatchBundle());
    }

    /**
     * Checks whether this move can be applied to the supplied world.
     *
     * @param world the world whose state is used or updated
     * @return a status indicating whether the move can be applied
     */
    @NotNull
    @Override
    public Status applicable(@NotNull UnmodifiableWorld world) {
        // need to check that there is not another station close by

        // check if station with id is already existing
        if (Utils.containsId(station.getId(), world.getStations(player))) {
            return Status.fail("Station with id already existing");
        }
        return Status.OK;
    }

    /**
     * Applies this move to the supplied world.
     *
     * @param world the world whose state is used or updated
     */
    @Override
    public void apply(@NotNull World world) {
        if (Utils.containsId(station.getId(), world.getStations(player))) {
            throw new RuntimeException("Station with id already existing");
        }
        world.addStation(player, new Station(station));
    }
}
