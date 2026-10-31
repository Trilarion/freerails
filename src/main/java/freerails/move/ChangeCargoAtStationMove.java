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

import freerails.model.operations.cargo.ImmutableCargoBatchBundle;
import freerails.model.operations.cargo.UnmodifiableCargoBatchBundle;
import freerails.model.player.Player;
import freerails.model.operations.station.Station;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.world.World;
import freerails.util.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Replaces the cargo batches stored at one of a player's stations.
 */
public class ChangeCargoAtStationMove implements Move {

    private final Player player;
    private final int stationId;
    private final UnmodifiableCargoBatchBundle cargoBatchBundle;

    /**
     * Creates a change cargo at station move from the supplied values.
     *
     * @param player the player
     * @param stationId the identifier to use
     * @param cargoBatchBundle the cargo batch bundle
     */
    public ChangeCargoAtStationMove(@NotNull Player player, int stationId, @NotNull UnmodifiableCargoBatchBundle cargoBatchBundle) {
        this.player = player;
        this.stationId = stationId;
        this.cargoBatchBundle = new ImmutableCargoBatchBundle(cargoBatchBundle);
    }

    /**
     * Returns whether this object is equal to the specified object.
     *
     * @param obj the object to compare with this object
     * @return true if this object is equal to the specified object, otherwise false
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ChangeCargoAtStationMove other)) {
            return false;
        }
        return stationId == other.stationId
                && Objects.equals(player, other.player)
                && Objects.equals(cargoBatchBundle, other.cargoBatchBundle);
    }

    /**
     * Returns a hash code for this object.
     *
     * @return the hash code for this object
     */
    @Override
    public int hashCode() {
        return Objects.hash(player, stationId, cargoBatchBundle);
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
        if (!Utils.containsId(stationId, world.getStations(player))) {
            return Status.fail("Station not existing.");
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
        Station station = world.getStation(player, stationId);
        station.setCargoBatchBundle(cargoBatchBundle);
    }
}
