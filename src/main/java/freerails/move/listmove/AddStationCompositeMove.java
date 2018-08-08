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

/*
 *
 */
package freerails.move.listmove;

import freerails.model.cargo.CargoBatchBundle;
import freerails.move.AddStationMove;
import freerails.move.CompositeMove;
import freerails.move.Move;
import freerails.util.Vec2D;
import freerails.model.world.PlayerKey;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.player.Player;
import freerails.model.station.Station;

/**
 * This {@link CompositeMove}adds a station to the station list and adds a
 * cargo bundle (to store the cargo waiting at the station) to the cargo bundle
 * list.
 */
public class AddStationCompositeMove extends CompositeMove {
    private static final long serialVersionUID = 3256728398461089080L;

    public AddStationCompositeMove(Move[] moves) {
        super(moves);
    }

    /**
     * @param world
     * @param stationName
     * @param location
     * @param upgradeTrackMove
     * @param player
     * @return
     */
    public static AddStationCompositeMove generateMove(UnmodifiableWorld world, String stationName, Vec2D location, Move upgradeTrackMove, Player player) {
        int cargoBundleNumber = world.size(player, PlayerKey.CargoBundles);
        Move addCargoBundleMove = new AddItemToListMove(PlayerKey.CargoBundles, cargoBundleNumber, CargoBatchBundle.EMPTY_CARGO_BATCH_BUNDLE, player);
        // TODO maybe a different way of getting new ids (more random, but without collisions)
        int stationNumber = world.getStations(player).size();
        // TODO the same as station number
        int id = world.getStations(player).size();
        Station station = new Station(id, location, stationName, world.getCargos().size(), cargoBundleNumber);
        Move addStation = new AddStationMove(player, station);

        return new AddStationCompositeMove(new Move[]{upgradeTrackMove, addCargoBundleMove, addStation});
    }

}