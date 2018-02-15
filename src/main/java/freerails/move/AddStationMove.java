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
package freerails.move;

import freerails.move.listmove.AddCargoBundleMove;
import freerails.move.listmove.AddItemToListMove;
import freerails.util.Vector2D;
import freerails.model.KEY;
import freerails.model.world.ReadOnlyWorld;
import freerails.model.SKEY;
import freerails.model.cargo.ImmutableCargoBatchBundle;
import freerails.model.player.FreerailsPrincipal;
import freerails.model.station.Station;

/**
 * This {@link CompositeMove}adds a station to the station list and adds a
 * cargo bundle (to store the cargo waiting at the station) to the cargo bundle
 * list.
 */
public class AddStationMove extends CompositeMove {
    private static final long serialVersionUID = 3256728398461089080L;

    private AddStationMove(Move[] moves) {
        super(moves);
    }

    /**
     * @param world
     * @param stationName
     * @param location
     * @param upgradeTrackMove
     * @param principal
     * @return
     */
    public static AddStationMove generateMove(ReadOnlyWorld world, String stationName, Vector2D location, Move upgradeTrackMove, FreerailsPrincipal principal) {
        int cargoBundleNumber = world.size(principal, KEY.CARGO_BUNDLES);
        Move addCargoBundleMove = new AddCargoBundleMove(cargoBundleNumber, ImmutableCargoBatchBundle.EMPTY_CARGO_BATCH_BUNDLE, principal);
        int stationNumber = world.size(principal, KEY.STATIONS);
        Station station = new Station(location, stationName, world.size(SKEY.CARGO_TYPES), cargoBundleNumber);

        Move addStation = new AddItemToListMove(KEY.STATIONS, stationNumber, station, principal);

        return new AddStationMove(new Move[]{upgradeTrackMove, addCargoBundleMove, addStation});
    }

    /**
     * @param upgradeTrackMove
     * @return
     */
    public static AddStationMove upgradeStation(Move upgradeTrackMove) {
        return new AddStationMove(new Move[]{upgradeTrackMove});
    }

}