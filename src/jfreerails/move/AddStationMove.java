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
import jfreerails.world.top.NonNullElements;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;
import jfreerails.world.top.World;
import jfreerails.world.top.WorldIterator;
import jfreerails.world.track.FreerailsTile;
import jfreerails.world.track.TrackPiece;
import jfreerails.world.track.TrackRule;


/**
 * This {@link CompositeMove}adds a station to the station list and adds a
 * cargo bundle (to store the cargo waiting at the station) to the cargo bundle
 * list.
 *
 * @author Luke
 *
 */
public class AddStationMove extends CompositeMove {
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

    /** This method may be called under 3 possible conditions: (1) when a station is getting built, (2) 
     * when a station is getting upgraded, (3) when a staton is getting removed.    
     */
    protected static MoveStatus check4overlap(World w, Point location, TrackPiece trackPiece) {
        /* Fix for 915945 (Stations should not overlap)
         * Check that there is not another station whose radius overlaps with
         * the one we are building.
         */        
        TrackRule thisStationType = trackPiece.getTrackRule();
        assert thisStationType.isStation();

        for (int player = 0; player < w.getNumberOfPlayers(); player++) {
            FreerailsPrincipal principal = w.getPlayer(player).getPrincipal();
            WorldIterator wi = new NonNullElements(KEY.STATIONS, w, principal);

            while (wi.next()) {
                StationModel station = (StationModel)wi.getElement();
                
                /* Fix for bug 948675 - Can't upgrade station types
                 * If locations are the same, then we are upgrading a station so
                 * it doesn't matter if the radii overlap.
                 */
                
                if(location.x == station.x && location.y == station.y){
                	continue;
                }
                
                FreerailsTile tile = (FreerailsTile)w.getTile(station.x,
                        station.y);
                TrackRule otherStationType = tile.getTrackRule();
                assert otherStationType.isStation();

                int sumOfRadii = otherStationType.getStationRadius() +
                    thisStationType.getStationRadius();
                int sumOfRadiiSquared = sumOfRadii * sumOfRadii;
                int xDistance = station.x - location.x;
                int yDistance = station.y - location.y;

                //Do radii overlap?	                
                boolean xOverlap = sumOfRadiiSquared >= (xDistance * xDistance);
                boolean yOverlap = sumOfRadiiSquared >= (yDistance * yDistance);
               
                if (xOverlap && yOverlap) {
                    String message = "Too close to " +
                        station.getStationName();

                    return MoveStatus.moveFailed(message);
                }
            }
        }

        return MoveStatus.MOVE_OK;
    }
}