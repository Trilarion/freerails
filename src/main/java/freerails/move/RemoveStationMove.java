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

package freerails.move;

import freerails.model.player.Player;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.world.World;
import org.jetbrains.annotations.NotNull;

// TODO hashcode, equals, unto, trydo...
/**
 *
 */
public class RemoveStationMove implements Move {

    private final Player player;
    private final int stationId;

    public RemoveStationMove(@NotNull Player player, int stationId) {
        this.player = player;
        this.stationId = stationId;
    }

    @NotNull
    @Override
    public Status applicable(@NotNull UnmodifiableWorld world) {
        return Status.OK;
    }

    @Override
    public void apply(@NotNull World world) {
        world.removeStation(player, stationId);
    }
}
