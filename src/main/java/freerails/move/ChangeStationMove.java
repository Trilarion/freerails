package freerails.move;

import freerails.world.player.FreerailsPrincipal;
import freerails.world.station.StationModel;
import freerails.world.top.KEY;

/**
 * This Move changes the properties of a station.
 *
 */
final public class ChangeStationMove extends ChangeItemInListMove {
    private static final long serialVersionUID = 3833469496064160307L;

    /**
     *
     * @param index
     * @param before
     * @param after
     * @param p
     */
    public ChangeStationMove(int index, StationModel before,
                             StationModel after, FreerailsPrincipal p) {
        super(KEY.STATIONS, index, before, after, p);
    }
}