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

import freerails.model.cargo.CargoBatchBundle;
import freerails.model.player.Player;
import freerails.model.station.Station;
import freerails.model.world.World;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public class ChangeCargoAtStationMove implements Move {

    private final Player player;
    private final int stationId;
    private final CargoBatchBundle cargoBatchBundle;

    public ChangeCargoAtStationMove(@NotNull Player player, int stationId, @NotNull CargoBatchBundle cargoBatchBundle) {
        this.player = player;
        this.stationId = stationId;
        this.cargoBatchBundle = cargoBatchBundle;
    }

    @Override
    public Status tryDoMove(World world, Player player) {
        return Status.OK;
    }

    @Override
    public Status tryUndoMove(World world, Player player) {
        return Status.OK;
    }

    @Override
    public Status doMove(World world, Player player) {
        Station station = world.getStation(this.player, stationId);
        station.setCargoBatchBundle(cargoBatchBundle);
        return Status.OK;
    }

    @Override
    public Status undoMove(World world, Player player) {
        return Status.OK;
    }
}
