package jfreerails.controller;

import java.awt.Point;
import java.util.logging.Logger;
import jfreerails.move.AddStationMove;
import jfreerails.move.ChangeTrackPieceCompositeMove;
import jfreerails.move.ChangeTrackPieceMove;
import jfreerails.move.Move;
import jfreerails.move.MoveStatus;
import jfreerails.move.TrackMoveTransactionsGenerator;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;
import jfreerails.world.track.FreerailsTile;
import jfreerails.world.track.NullTrackType;
import jfreerails.world.track.TrackPiece;
import jfreerails.world.track.TrackRule;


/**
 * Class to build a station at a given point, names station after nearest
 * city. If that name is taken then a "Junction" or "Siding" is added to
 * the name.
 * @author Luke Lindsay 08-Nov-2002
 *
 * Updated 12th April 2003 by Scott Bennett to include nearest city names.
 *
 */
public class StationBuilder {
    private static final Logger logger = Logger.getLogger(StationBuilder.class.getName());
    private int ruleNumber;
    private final TrackMoveTransactionsGenerator transactionsGenerator;
    private final MoveExecutor executor;

    public StationBuilder(MoveExecutor executor) {
        this.executor = executor;

        TrackRule trackRule;

        int i = -1;

        ReadOnlyWorld world = executor.getWorld();

        do {
            i++;
            trackRule = (TrackRule)world.get(SKEY.TRACK_RULES, i);
        } while (!trackRule.isStation());

        ruleNumber = i;

        FreerailsPrincipal principal = executor.getPrincipal();
        transactionsGenerator = new TrackMoveTransactionsGenerator(world,
                principal);
    }

    public boolean canBuiltStationHere(Point p) {
        ReadOnlyWorld world = executor.getWorld();
        FreerailsTile oldTile = world.getTile(p.x, p.y);
        TrackRule oldTrackRule = oldTile.getTrackRule();

        return !oldTrackRule.equals(NullTrackType.getInstance());
    }

    public MoveStatus buildStation(Point p) {
        ReadOnlyWorld world = executor.getWorld();
        FreerailsTile oldTile = world.getTile(p.x, p.y);

        //Only build a station if there is track at the specified point.
        if (canBuiltStationHere(p)) {
            String cityName;
            String stationName;

            TrackPiece before = world.getTile(p.x, p.y);
            TrackRule trackRule = (TrackRule)world.get(SKEY.TRACK_RULES,
                    this.ruleNumber);

            FreerailsPrincipal principal = executor.getPrincipal();
            int owner = ChangeTrackPieceCompositeMove.getOwner(principal, world);
            TrackPiece after = trackRule.getTrackPiece(before.getTrackConfiguration(),
                    owner);
            ChangeTrackPieceMove upgradeTrackMove = new ChangeTrackPieceMove(before,
                    after, p);

            //Check whether we can upgrade the track to a station here.
            MoveStatus statusa = executor.tryDoMove(upgradeTrackMove);

            if (!statusa.ok) {
                logger.warning("Cannot upgrade this track to a station!");

                return statusa;
            }

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

            return executor.doMove(move);
        } else {
            String message = "Can't build station since there is no track here!";
            logger.warning(message);

            return MoveStatus.moveFailed(message);
        }
    }

    public void setStationType(int ruleNumber) {
        this.ruleNumber = ruleNumber;
    }
}