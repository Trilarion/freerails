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
import freerails.model.station.Station;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.world.World;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public class UpdateStationCargoDemandSupplyConversionMove implements Move {

    private final Player player;
    private final Station station;

    public UpdateStationCargoDemandSupplyConversionMove(@NotNull Player player, @NotNull Station station) {
        this.player = player;
        this.station = station;
    }

    @NotNull
    @Override
    public Status applicable(@NotNull UnmodifiableWorld world) {
        return Status.OK;
    }

    @Override
    public void apply(@NotNull World world) {
        Station oldStation = world.getStation(player, station.getId());
        oldStation.setSupply(station.getSupply());
        oldStation.setCargoConversion(station.getCargoConversion());
        oldStation.setDemandForCargo(station.getDemandForCargo());
    }
}
