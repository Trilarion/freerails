package jfreerails.client.common;

import jfreerails.controller.ModelRoot;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.track.FreerailsTile;
import jfreerails.world.track.TrackRule;


public class StationHelper {

    /**
     * Return Station number if station exists at location or -1
     * 
     * @param target
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
