/*
 * Created on 19-Oct-2004
 *
 */
package jfreerails.controller;

import java.awt.Point;
import java.util.NoSuchElementException;

import jfreerails.move.AddItemToListMove;
import jfreerails.move.AddStationMove;
import jfreerails.move.ChangeTrackPieceCompositeMove;
import jfreerails.move.ChangeTrackPieceMove;
import jfreerails.move.CompositeMove;
import jfreerails.move.Move;
import jfreerails.move.TrackMoveTransactionsGenerator;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.station.StationModel;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.top.SKEY;
import jfreerails.world.track.FreerailsTile;
import jfreerails.world.track.TrackPiece;
import jfreerails.world.track.TrackPieceImpl;
import jfreerails.world.track.TrackRule;


/**
 * Generates a move that adds or upgrades a station. 
 * 
 * @author Luke
 *
 */
public class AddStationPreMove implements PreMove {
    
    private final Point p;
    private final int ruleNumber;
    private final FreerailsPrincipal principal;

    private AddStationPreMove(Point p, int trackRule,
        FreerailsPrincipal principal) {    
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
                trackRule, owner, ruleNumber);
        ChangeTrackPieceMove upgradeTrackMove = new ChangeTrackPieceMove(before,
                after, p);

        CompositeMove move;

        if (!oldTile.getTrackRule().isStation()) {
            //There isn't already a station here, we need to pick a name and add an entry
            //to the station list.
        	CalcNearestCity cNC = new CalcNearestCity(world, p.x, p.y);
        	try{
        		cityName = cNC.findNearestCity();
        		
        		VerifyStationName vSN = new VerifyStationName(world, cityName);
        		stationName = vSN.getName();
        		
        	}catch(NoSuchElementException e){
        		//there are no cities, this should never happen during a proper game.  However
        		//some of the unit tests create stations when there are no cities.
        		stationName = "Central Station #"+world.size(KEY.STATIONS, principal);
        	}

            //check the terrain to see if we can build a station on it...
            move = AddStationMove.generateMove(world, stationName, p,
                    upgradeTrackMove, principal);
            move = addSupplyAndDemand(move, world);
            move = transactionsGenerator.addTransactions(move);
        } else {
            //Upgrade an existing station.
            move = AddStationMove.upgradeStation(upgradeTrackMove);
        }

        return move;
    }

    private CompositeMove addSupplyAndDemand(CompositeMove m, ReadOnlyWorld w) {
        Move[] moves = m.getMoves();

        for (int i = 0; i < moves.length; i++) {
            if (moves[i] instanceof AddItemToListMove) {
                AddItemToListMove move = (AddItemToListMove)moves[i];

                if (move.getKey().equals(KEY.STATIONS)) {
                    StationModel station = (StationModel)move.getAfter();
                    CalcCargoSupplyRateAtStation supplyRate;
                    supplyRate = new CalcCargoSupplyRateAtStation(w, station.x,
                            station.y, ruleNumber);

                    StationModel stationAfter = supplyRate.calculations(station);
                    moves[i] = new AddItemToListMove(move.getKey(),
                            move.getIndex(), stationAfter, move.getPrincipal());
                }
            }
        }

        return new CompositeMove(moves);
    }
}