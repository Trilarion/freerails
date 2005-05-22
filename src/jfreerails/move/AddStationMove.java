/*
 * Created on 26-May-2003
 *
 */
package jfreerails.move;

import java.awt.Point;

import jfreerails.world.cargo.ImmutableCargoBundle;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;


/**
 * This {@link CompositeMove}adds a station to the station list and adds a
 * cargo bundle (to store the cargo waiting at the station) to the cargo bundle
 * list.
 *
 * @author Luke
 *
 */
public class AddStationMove extends CompositeMove {
    private static final long serialVersionUID = 3256728398461089080L;

	private AddStationMove(Move[] moves) {
        super(moves);
    }

    public StationModel getNewStation() {
        AddItemToListMove addStation = (AddItemToListMove)super.getMove(2);

        return (StationModel)addStation.getAfter();
    }

    public static AddStationMove generateMove(ReadOnlyWorld w,
        String stationName, Point p, ChangeTrackPieceMove upgradeTrackMove,
        FreerailsPrincipal principal) {
        int cargoBundleNumber = w.size(KEY.CARGO_BUNDLES, principal);
        Move addCargoBundleMove = new AddCargoBundleMove(cargoBundleNumber,
                ImmutableCargoBundle.EMPTY_BUNDLE, principal);
        int stationNumber = w.size(KEY.STATIONS, principal);
        StationModel station = new StationModel(p.x, p.y, stationName,
                w.size(SKEY.CARGO_TYPES), cargoBundleNumber);

        Move addStation = new AddItemToListMove(KEY.STATIONS, stationNumber,
                station, principal);

        return new AddStationMove(new Move[] {
                upgradeTrackMove, addCargoBundleMove, addStation
            });
    }

    public static AddStationMove upgradeStation(
        ChangeTrackPieceMove upgradeTrackMove) {
        return new AddStationMove(new Move[] {upgradeTrackMove});
    }
}