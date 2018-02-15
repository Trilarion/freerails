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
 *
 */
package freerails.move.premove;

import freerails.controller.CalcCargoSupplyRateAtStation;
import freerails.controller.NearestCityFinder;
import freerails.controller.VerifyStationName;
import freerails.move.*;
import freerails.move.listmove.AddItemToListMove;
import freerails.move.mapupdatemove.ChangeTrackPieceCompositeMove;
import freerails.move.mapupdatemove.ChangeTrackPieceMove;
import freerails.util.Vector2D;
import freerails.model.world.WorldKey;
import freerails.model.world.ReadOnlyWorld;
import freerails.model.world.WorldSharedKey;
import freerails.model.player.FreerailsPrincipal;
import freerails.model.station.Station;
import freerails.model.terrain.FullTerrainTile;
import freerails.model.track.TrackPiece;
import freerails.model.track.TrackPieceImpl;
import freerails.model.track.TrackRule;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Generates a move that adds or upgrades a station.
 */
public class AddStationPreMove implements PreMove {

    private static final long serialVersionUID = 3258131349411148085L;
    private final Vector2D p;
    private final int ruleNumber;
    private final FreerailsPrincipal principal;

    private AddStationPreMove(Vector2D p, int trackRule, FreerailsPrincipal principal) {
        this.p = p;
        ruleNumber = trackRule;
        this.principal = principal;
    }

    /**
     * @param p
     * @param trackRule
     * @param principal
     * @return
     */
    public static AddStationPreMove newStation(Vector2D p, int trackRule, FreerailsPrincipal principal) {
        return new AddStationPreMove(p, trackRule, principal);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof AddStationPreMove)) return false;

        final AddStationPreMove addStationPreMove = (AddStationPreMove) obj;

        if (ruleNumber != addStationPreMove.ruleNumber) return false;
        if (!p.equals(addStationPreMove.p)) return false;
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
        TrackMoveTransactionsGenerator transactionsGenerator = new TrackMoveTransactionsGenerator(world, principal);

        FullTerrainTile oldTile = (FullTerrainTile) world.getTile(p);
        String cityName;
        String stationName;

        FullTerrainTile ft = (FullTerrainTile) world.getTile(p);
        TrackPiece before = ft.getTrackPiece();
        TrackRule trackRule = (TrackRule) world.get(WorldSharedKey.TrackRules, ruleNumber);

        int owner = ChangeTrackPieceCompositeMove.getOwner(principal, world);
        TrackPiece after = new TrackPieceImpl(before.getTrackConfiguration(), trackRule, owner, ruleNumber);
        Move upgradeTrackMove = new ChangeTrackPieceMove(before, after, p);

        CompositeMove move;

        if (!oldTile.getTrackPiece().getTrackRule().isStation()) {
            // There isn't already a station here, we need to pick a name and
            // add an entry to the station list.
            NearestCityFinder nearestCityFinder = new NearestCityFinder(world, p);
            try {
                cityName = nearestCityFinder.findNearestCity();

                VerifyStationName vSN = new VerifyStationName(world, cityName);
                stationName = vSN.getName();
            } catch (NoSuchElementException e) {
                // there are no cities, this should never happen during a proper
                // game. However
                // some of the unit tests create stations when there are no
                // cities.
                stationName = "Central Station #" + world.size(principal, WorldKey.Stations);
            }

            // check the terrain to see if we can build a station on it...
            move = AddStationMove.generateMove(world, stationName, p, upgradeTrackMove, principal);
            move = addSupplyAndDemand(move, world);
            move = transactionsGenerator.addTransactions(move);
        } else {
            // Upgrade an existing station.
            move = AddStationMove.upgradeStation(upgradeTrackMove);
        }

        return move;
    }

    private CompositeMove addSupplyAndDemand(CompositeMove m, ReadOnlyWorld world) {
        List<Move> moves2 = m.getMoves();
        Move[] moves = new Move[moves2.size()];
        for (int i = 0; i < moves2.size(); i++) {
            moves[i] = moves2.get(i);
        }

        for (int i = 0; i < moves.length; i++) {
            if (moves[i] instanceof AddItemToListMove) {
                AddItemToListMove move = (AddItemToListMove) moves[i];

                if (move.getKey().equals(WorldKey.Stations)) {
                    Station station = (Station) move.getAfter();
                    CalcCargoSupplyRateAtStation supplyRate;
                    supplyRate = new CalcCargoSupplyRateAtStation(world, station.location, ruleNumber);
                    Station stationAfter = supplyRate.calculations(station);
                    moves[i] = new AddItemToListMove(move.getKey(), move.getIndex(), stationAfter, move.getPrincipal());
                }
            }
        }

        return new CompositeMove(moves);
    }
}