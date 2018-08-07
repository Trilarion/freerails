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
import freerails.model.track.TrackConfiguration;
import freerails.model.track.TrackType;
import freerails.move.*;
import freerails.move.listmove.AddItemToListMove;
import freerails.move.listmove.AddStationCompositeMove;
import freerails.move.mapupdatemove.ChangeTrackPieceMove;
import freerails.util.Vec2D;
import freerails.model.world.PlayerKey;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.player.Player;
import freerails.model.station.Station;
import freerails.model.terrain.TerrainTile;
import freerails.model.track.TrackPiece;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Generates a move that adds or upgrades a station.
 */
public class AddStationMoveGenerator implements MoveGenerator {

    private static final long serialVersionUID = 3258131349411148085L;
    private final Vec2D location;
    private final int ruleNumber;
    private final Player player;

    private AddStationMoveGenerator(Vec2D location, int trackRule, Player player) {
        this.location = location;
        ruleNumber = trackRule;
        this.player = player;
    }

    /**
     * @param p
     * @param trackRule
     * @param player
     * @return
     */
    public static AddStationMoveGenerator newStation(Vec2D p, int trackRule, Player player) {
        return new AddStationMoveGenerator(p, trackRule, player);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof AddStationMoveGenerator)) return false;

        final AddStationMoveGenerator addStationPreMove = (AddStationMoveGenerator) obj;

        if (ruleNumber != addStationPreMove.ruleNumber) return false;
        if (!location.equals(addStationPreMove.location)) return false;
        return player.equals(addStationPreMove.player);
    }

    @Override
    public int hashCode() {
        int result;
        result = location.hashCode();
        result = 29 * result + ruleNumber;
        result = 29 * result + player.hashCode();
        return result;
    }

    /**
     * @param world
     * @return
     */
    public Move generate(UnmodifiableWorld world) {
        TrackMoveTransactionsGenerator transactionsGenerator = new TrackMoveTransactionsGenerator(world, player);

        TerrainTile oldTile = (TerrainTile) world.getTile(location);
        String cityName;
        String stationName;

        TerrainTile ft = (TerrainTile) world.getTile(location);
        TrackPiece before = ft.getTrackPiece();
        TrackType trackType = world.getTrackType(ruleNumber);

        int owner = player.getId();
        TrackPiece after = new TrackPiece(before == null ? TrackConfiguration.from9bitTemplate(0) : before.getTrackConfiguration(), trackType, owner);
        Move upgradeTrackMove = new ChangeTrackPieceMove(before, after, location);

        CompositeMove move;

        if (oldTile.getTrackPiece() == null || !oldTile.getTrackPiece().getTrackType().isStation()) {
            // There isn't already a station here, we need to pick a name and
            // add an entry to the station list.
            NearestCityFinder nearestCityFinder = new NearestCityFinder(world, location);
            try {
                cityName = nearestCityFinder.findNearestCity();

                VerifyStationName vSN = new VerifyStationName(world, cityName);
                stationName = vSN.getName();
            } catch (NoSuchElementException e) {
                // TODO can we do better here?
                // there are no cities, this should never happen during a proper
                // game. However, some of the unit tests create stations when there are no cities.
                stationName = "Central Station #" + world.getStations(player).size();
            }

            // check the terrain to see if we can build a station on it...
            move = AddStationCompositeMove.generateMove(world, stationName, location, upgradeTrackMove, player);
            move = addSupplyAndDemand(move, world);
            move = transactionsGenerator.addTransactions(move);
        } else {
            // Upgrade an existing station.
            move = new AddStationCompositeMove(new Move[]{upgradeTrackMove});
        }

        return move;
    }

    // TODO frankly, this looks like a hack, moves are modified after their creation
    private CompositeMove addSupplyAndDemand(CompositeMove compositeMove, UnmodifiableWorld world) {
        List<Move> moves2 = compositeMove.getMoves();
        Move[] moves = new Move[moves2.size()];
        for (int i = 0; i < moves2.size(); i++) {
            moves[i] = moves2.get(i);
        }

        for (int i = 0; i < moves.length; i++) {
            if (moves[i] instanceof AddStationMove) {
                AddStationMove move = (AddStationMove) moves[i];

                Station station = move.getStation();
                CalculateCargoSupplyRateAtStation supplyRate;
                supplyRate = new CalculateCargoSupplyRateAtStation(world, station.location, ruleNumber);
                Station stationAfter = supplyRate.calculations(station);
                moves[i] = new AddStationMove(move.getPlayer(), stationAfter);
            }
        }

        return new CompositeMove(moves);
    }
}