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

// TODO hashcode, equals, try, undo ...
/**
 *
 */
public class AddStationMove implements Move {

    private final Player player;
    private final Station station;

    public AddStationMove(@NotNull Player player, @NotNull Station station) {
        this.player = player;
        this.station = station;
    }

    public Station getStation() {
        return station;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AddStationMove)) return false;
        final AddStationMove other = (AddStationMove) o;

        return player.equals(other.player) && station.equals(other.station);
    }

    @Override
    public int hashCode() {
        int result = player.hashCode();
        result = 29 * result + station.hashCode();
        return result;
    }

    @Override
    public Status applicable(UnmodifiableWorld world) {
        if (World.contains(station.getId(), world.getStations(player))) {
            return Status.fail("Station with id already existing");
        }
        return Status.OK;
    }

    @Override
    public void apply(World world) {
        if (World.contains(station.getId(), world.getStations(player))) {
            throw new RuntimeException("Station with id already existing");
        }
        world.addStation(player, station);
    }
}
