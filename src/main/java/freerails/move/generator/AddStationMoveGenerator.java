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
package freerails.move.generator;

import freerails.model.station.CalculateCargoSupplyRateAtStation;
import freerails.model.terrain.NearestCityFinder;
import freerails.model.station.VerifyStationName;
import freerails.move.*;
import freerails.move.listmove.AddItemToListMove;
import freerails.move.listmove.AddStationMove;
import freerails.move.mapupdatemove.ChangeTrackPieceCompositeMove;
import freerails.move.mapupdatemove.ChangeTrackPieceMove;
import freerails.util.Vec2D;
import freerails.model.world.PlayerKey;
import freerails.model.world.ReadOnlyWorld;
import freerails.model.world.SharedKey;
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
public class AddStationMoveGenerator implements MoveGenerator {

    private static final long serialVersionUID = 3258131349411148085L;
    private final Vec2D location;
    private final int ruleNumber;
    private final FreerailsPrincipal principal;

    private AddStationMoveGenerator(Vec2D location, int trackRule, FreerailsPrincipal principal) {
        this.location = location;
        ruleNumber = trackRule;
        this.principal = principal;
    }

    /**
     * @param p
     * @param trackRule
     * @param principal
     * @return
     */
    public static AddStationMoveGenerator newStation(Vec2D p, int trackRule, FreerailsPrincipal principal) {
        return new AddStationMoveGenerator(p, trackRule, principal);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof AddStationMoveGenerator)) return false;

        final AddStationMoveGenerator addStationPreMove = (AddStationMoveGenerator) obj;

        if (ruleNumber != addStationPreMove.ruleNumber) return false;
        if (!location.equals(addStationPreMove.location)) return false;
        return principal.equals(addStationPreMove.principal);
    }

    @Override
    public int hashCode() {
        int result;
        result = location.hashCode();
        result = 29 * result + ruleNumber;
        result = 29 * result + principal.hashCode();
        return result;
    }

    /**
     * @param world
     * @return
     */
    public Move generate(ReadOnlyWorld world) {
        TrackMoveTransactionsGenerator transactionsGenerator = new TrackMoveTransactionsGenerator(world, principal);

        FullTerrainTile oldTile = (FullTerrainTile) world.getTile(location);
        String cityName;
        String stationName;

        FullTerrainTile ft = (FullTerrainTile) world.getTile(location);
        TrackPiece before = ft.getTrackPiece();
        TrackRule trackRule = (TrackRule) world.get(SharedKey.TrackRules, ruleNumber);

        int owner = ChangeTrackPieceCompositeMove.getOwner(principal, world);
        TrackPiece after = new TrackPieceImpl(before.getTrackConfiguration(), trackRule, owner, ruleNumber);
        Move upgradeTrackMove = new ChangeTrackPieceMove(before, after, location);

        CompositeMove move;

        if (!oldTile.getTrackPiece().getTrackRule().isStation()) {
            // There isn't already a station here, we need to pick a name and
            // add an entry to the station list.
            NearestCityFinder nearestCityFinder = new NearestCityFinder(world, location);
            try {
                cityName = nearestCityFinder.findNearestCity();

                VerifyStationName vSN = new VerifyStationName(world, cityName);
                stationName = vSN.getName();
            } catch (NoSuchElementException e) {
                // there are no cities, this should never happen during a proper
                // game. However
                // some of the unit tests create stations when there are no
                // cities.
                stationName = "Central Station #" + world.size(principal, PlayerKey.Stations);
            }

            // check the terrain to see if we can build a station on it...
            move = AddStationMove.generateMove(world, stationName, location, upgradeTrackMove, principal);
            move = addSupplyAndDemand(move, world);
            move = transactionsGenerator.addTransactions(move);
        } else {
            // Upgrade an existing station.
            move = AddStationMove.upgradeStation(upgradeTrackMove);
        }

        return move;
    }

    private CompositeMove addSupplyAndDemand(CompositeMove compositeMove, ReadOnlyWorld world) {
        List<Move> moves2 = compositeMove.getMoves();
        Move[] moves = new Move[moves2.size()];
        for (int i = 0; i < moves2.size(); i++) {
            moves[i] = moves2.get(i);
        }

        for (int i = 0; i < moves.length; i++) {
            if (moves[i] instanceof AddItemToListMove) {
                AddItemToListMove move = (AddItemToListMove) moves[i];

                if (move.getKey().equals(PlayerKey.Stations)) {
                    Station station = (Station) move.getAfter();
                    CalculateCargoSupplyRateAtStation supplyRate;
                    supplyRate = new CalculateCargoSupplyRateAtStation(world, station.location, ruleNumber);
                    Station stationAfter = supplyRate.calculations(station);
                    moves[i] = new AddItemToListMove(move.getKey(), move.getIndex(), stationAfter, move.getPrincipal());
                }
            }
        }

        return new CompositeMove(moves);
    }
}