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

package freerails.client.common;

import freerails.controller.ModelRoot;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.station.StationModel;
import freerails.world.top.KEY;
import freerails.world.top.ReadOnlyWorld;
import freerails.world.track.FreerailsTile;
import freerails.world.track.TrackRule;

/**
 *
 */
public class StationHelper {

    /**
     * Return Station number if station exists at location or -1
     *
     * @param world
     * @param modelRoot
     * @param x
     * @param y
     * @return
     */
    public static int getStationNumberAtLocation(ReadOnlyWorld world, ModelRoot modelRoot, int x, int y) {
        FreerailsTile tile = (FreerailsTile) world.getTile(x, y);

        TrackRule trackRule = tile.getTrackPiece().getTrackRule();
        FreerailsPrincipal principal = modelRoot.getPrincipal();
        if (trackRule.isStation()
                && tile.getTrackPiece().getOwnerID() == world.getID(principal)) {

            for (int i = 0; i < world.size(principal, KEY.STATIONS); i++) {
                StationModel station = (StationModel) world.get(principal,
                        KEY.STATIONS, i);

                if (null != station && station.x == x && station.y == y) {
                    return i;
                }
            }

//            throw new IllegalStateException("Couldn't find station at " + x
//                    + ", " + y);
        }
        return -1;
        // Don't show terrain...
    }
}
