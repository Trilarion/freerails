package jfreerails.move;

import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;


/**
 *
 * This Move changes the properties of a station.
 *
 * @author lindsal
 */
final public class ChangeStationMove extends ChangeItemInListMove {
    public ChangeStationMove(int index, StationModel before,
        StationModel after, FreerailsPrincipal p) {
        super(KEY.STATIONS, index, before, after, p);
    }
}