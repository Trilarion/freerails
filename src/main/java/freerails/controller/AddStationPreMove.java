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
 * Created on 19-Oct-2004
 *
 */
package freerails.controller;

import freerails.move.*;
import freerails.util.ImList;
import freerails.util.ImPoint;
import freerails.world.KEY;
import freerails.world.ReadOnlyWorld;
import freerails.world.SKEY;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.station.StationModel;
import freerails.world.terrain.FreerailsTile;
import freerails.world.track.TrackPiece;
import freerails.world.track.TrackPieceImpl;
import freerails.world.track.TrackRule;

import java.util.NoSuchElementException;

/**
 * Generates a move that adds or upgrades a station.
 */
public class AddStationPreMove implements PreMove {

    private static final long serialVersionUID = 3258131349411148085L;

    private final ImPoint p;

    private final int ruleNumber;

    private final FreerailsPrincipal principal;

    private AddStationPreMove(ImPoint p, int trackRule,
                              FreerailsPrincipal principal) {
        this.p = p;
        this.ruleNumber = trackRule;
        this.principal = principal;
    }

    /**
     * @param p
     * @param trackRule
     * @param principal
     * @return
     */
    public static AddStationPreMove newStation(ImPoint p, int trackRule,
                                               FreerailsPrincipal principal) {
        return new AddStationPreMove(p, trackRule, principal);
    }

    /**
     * @param p
     * @param trackRule
     * @param principal
     * @return
     */
    public static AddStationPreMove upgradeStation(ImPoint p, int trackRule,
                                                   FreerailsPrincipal principal) {
        return new AddStationPreMove(p, trackRule, principal);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AddStationPreMove))
            return false;

        final AddStationPreMove addStationPreMove = (AddStationPreMove) o;

        if (ruleNumber != addStationPreMove.ruleNumber)
            return false;
        if (!p.equals(addStationPreMove.p))
            return false;
        return principal.equals(addStationPreMove.principal);
    }

    @Override
    public int hashCode() {
        int result;
        result = p.hashCode();
        result = 29 * result + ruleNumber;
        result = 29 * result + principal.hashCode();
        return result;
    }

    /**
     * @param world
     * @return
     */
    public Move generateMove(ReadOnlyWorld world) {
        TrackMoveTransactionsGenerator transactionsGenerator = new TrackMoveTransactionsGenerator(
                world, principal);

        FreerailsTile oldTile = (FreerailsTile) world.getTile(p.x, p.y);
        String cityName;
        String stationName;

        FreerailsTile ft = (FreerailsTile) world.getTile(p.x, p.y);
        TrackPiece before = ft.getTrackPiece();
        TrackRule trackRule = (TrackRule) world.get(SKEY.TRACK_RULES,
                this.ruleNumber);

        int owner = ChangeTrackPieceCompositeMove.getOwner(principal, world);
        TrackPiece after = new TrackPieceImpl(before.getTrackConfiguration(),
                trackRule, owner, ruleNumber);
        ChangeTrackPieceMove upgradeTrackMove = new ChangeTrackPieceMove(
                before, after, p);

        CompositeMove move;

        if (!oldTile.getTrackPiece().getTrackRule().isStation()) {
            // There isn't already a station here, we need to pick a name and
            // add an entry
            // to the station list.
            NearestCityFinder cNC = new NearestCityFinder(world, p.x, p.y);
            try {
                cityName = cNC.findNearestCity();

                VerifyStationName vSN = new VerifyStationName(world, cityName);
                stationName = vSN.getName();

            } catch (NoSuchElementException e) {
                // there are no cities, this should never happen during a proper
                // game. However
                // some of the unit tests create stations when there are no
                // cities.
                stationName = "Central Station #"
                        + world.size(principal, KEY.STATIONS);
            }

            // check the terrain to see if we can build a station on it...
            move = AddStationMove.generateMove(world, stationName, p,
                    upgradeTrackMove, principal);
            move = addSupplyAndDemand(move, world);
            move = transactionsGenerator.addTransactions(move);
        } else {
            // Upgrade an existing station.
            move = AddStationMove.upgradeStation(upgradeTrackMove);
        }

        return move;
    }

    private CompositeMove addSupplyAndDemand(CompositeMove m, ReadOnlyWorld w) {
        ImList<Move> moves2 = m.getMoves();
        Move[] moves = new Move[moves2.size()];
        for (int i = 0; i < moves2.size(); i++) {
            moves[i] = moves2.get(i);
        }

        for (int i = 0; i < moves.length; i++) {
            if (moves[i] instanceof AddItemToListMove) {
                AddItemToListMove move = (AddItemToListMove) moves[i];

                if (move.getKey().equals(KEY.STATIONS)) {
                    StationModel station = (StationModel) move.getAfter();
                    CalcCargoSupplyRateAtStation supplyRate;
                    supplyRate = new CalcCargoSupplyRateAtStation(w, station.x,
                            station.y, ruleNumber);

                    StationModel stationAfter = supplyRate
                            .calculations(station);
                    moves[i] = new AddItemToListMove(move.getKey(), move
                            .getIndex(), stationAfter, move.getPrincipal());
                }
            }
        }

        return new CompositeMove(moves);
    }
}