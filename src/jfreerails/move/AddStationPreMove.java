/*
 * Created on 19-Oct-2004
 *
 */
package jfreerails.move;

import java.awt.Point;
import jfreerails.controller.CalcNearestCity;
import jfreerails.controller.VerifyStationName;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;
import jfreerails.world.track.FreerailsTile;
import jfreerails.world.track.TrackPiece;
import jfreerails.world.track.TrackPieceImpl;
import jfreerails.world.track.TrackRule;


/**
 * @author Luke
 *
 */
public class AddStationPreMove implements PreMove {
    // private final String stationName;
    private final Point p;
    private final int ruleNumber;
    private final FreerailsPrincipal principal;

    private AddStationPreMove(Point p, int trackRule,
        FreerailsPrincipal principal) {
        //  this.stationName = stationName;
        this.p = p;
        this.ruleNumber = trackRule;
        this.principal = principal;
    }

    public static AddStationPreMove newStation(Point p, int trackRule,
        FreerailsPrincipal principal) {
        return new AddStationPreMove(p, trackRule, principal);
    }

    public static AddStationPreMove upgradeStation(Point p, int trackRule,
        FreerailsPrincipal principal) {
        return new AddStationPreMove(p, trackRule, principal);
    }

    public Move generateMove(ReadOnlyWorld world) {
        TrackMoveTransactionsGenerator transactionsGenerator = new TrackMoveTransactionsGenerator(world,
                principal);

        FreerailsTile oldTile = (FreerailsTile)world.getTile(p.x, p.y);
        String cityName;
        String stationName;

        TrackPiece before = (TrackPiece)world.getTile(p.x, p.y);
        TrackRule trackRule = (TrackRule)world.get(SKEY.TRACK_RULES,
                this.ruleNumber);

        int owner = ChangeTrackPieceCompositeMove.getOwner(principal, world);
        TrackPiece after = new TrackPieceImpl(before.getTrackConfiguration(),
                trackRule, owner);
        ChangeTrackPieceMove upgradeTrackMove = new ChangeTrackPieceMove(before,
                after, p);

        Move move;

        if (!oldTile.getTrackRule().isStation()) {
            //There isn't already a station here, we need to pick a name and add an entry
            //to the station list.
            CalcNearestCity cNC = new CalcNearestCity(world, p.x, p.y);
            cityName = cNC.findNearestCity();

            VerifyStationName vSN = new VerifyStationName(world, cityName);
            stationName = vSN.getName();

            if (stationName == null) {
                //there are no cities, this should never happen
                stationName = "Central Station";
            }

            //check the terrain to see if we can build a station on it...
            move = AddStationMove.generateMove(world, stationName, p,
                    upgradeTrackMove, principal);

            move = transactionsGenerator.addTransactions(move);
        } else {
            //Upgrade an existing station.
            move = AddStationMove.upgradeStation(upgradeTrackMove);
        }

        return move;
    }
}