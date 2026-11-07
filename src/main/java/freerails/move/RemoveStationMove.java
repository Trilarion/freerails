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
import freerails.model.world.UnmodifiableWorld;
import freerails.model.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Removes the specified station from the world.
 */
public class RemoveStationMove implements Move {

    private final Player player;
    private final int stationId;

    /**
     * Creates a remove station move from the supplied values.
     *
     * @param player the player
     * @param stationId the identifier to use
     */
    public RemoveStationMove(@NotNull Player player, int stationId) {
        this.player = player;
        this.stationId = stationId;
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
        if (!(obj instanceof RemoveStationMove other)) {
            return false;
        }
        return stationId == other.stationId && Objects.equals(player, other.player);
    }

    /**
     * Returns a hash code for this object.
     *
     * @return the hash code for this object
     */
    @Override
    public int hashCode() {
        return Objects.hash(player, stationId);
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
        return Status.OK;
    }

    /**
     * Applies this move to the supplied world.
     *
     * @param world the world whose state is used or updated
     */
    @Override
    public void apply(@NotNull World world) {
        world.removeStation(player, stationId);
    }
}
