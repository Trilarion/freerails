/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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

import freerails.model.operations.cargo.CargoBatchBundle;
import freerails.model.operations.station.StationUtils;
import freerails.model.terrain.city.CityUtils;
import freerails.model.track.TrackConfiguration;
import freerails.model.track.TrackType;
import freerails.move.*;
import freerails.move.mapupdatemove.ChangeTrackPieceMove;
import freerails.util.Vec2D;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.player.Player;
import freerails.model.operations.station.Station;
import freerails.model.terrain.TerrainTile;
import freerails.model.track.TrackPiece;

import java.io.Serial;
import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * Generates a move that adds or upgrades a station.
 */
public class AddStationMoveGenerator implements MoveGenerator {

    @Serial
    private static final long serialVersionUID = 3258131349411148085L;
    private final Vec2D location;
    private final int ruleNumber;
    private final Player player;

    /**
     * Creates an add station move generator from the supplied values.
     *
     * @param location the location
     * @param trackRule the track rule
     * @param player the player
     */
    public AddStationMoveGenerator(Vec2D location, int trackRule, Player player) {
        this.location = location;
        ruleNumber = trackRule;
        this.player = player;
    }

    /**
     * Returns whether this object is equal to the specified object.
     *
     * @param obj the object to compare with this object
     * @return true if this object is equal to the specified object, otherwise false
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof AddStationMoveGenerator addStationPreMove)) return false;

        if (ruleNumber != addStationPreMove.ruleNumber) return false;
        if (!location.equals(addStationPreMove.location)) return false;
        return player.equals(addStationPreMove.player);
    }

    /**
     * Returns a hash code for this object.
     *
     * @return the hash code for this object
     */
    @Override
    public int hashCode() {
        int result;
        result = location.hashCode();
        result = 29 * result + ruleNumber;
        result = 29 * result + player.hashCode();
        return result;
    }

    // TODO how often is this called (seems to be called every single time the mouse is over a tile)
    /**
     * @param world the game world used by this operation
     * @return the move that adds the station to the world
     */
    @Override
    public Move generate(UnmodifiableWorld world) {
        TerrainTile oldTile = world.getTile(location);
        TrackPiece oldTrackPiece = oldTile.trackPiece();
        if (oldTile.trackPiece() != null && oldTile.trackPiece().trackType().isStation()) {
            throw new RuntimeException("Station already at location");
        }
        TrackType trackType = world.getTrackType(ruleNumber);

        TrackPiece newTrackPiece = new TrackPiece(oldTrackPiece == null ? TrackConfiguration.from9bitTemplate(0) : oldTrackPiece.trackConfiguration(), trackType, player.getId());
        Move upgradeTrackMove = new ChangeTrackPieceMove(oldTrackPiece, newTrackPiece, location);

        // There isn't already a station here, we need to pick a name and add an entry to the station list.
        String stationName;
        try {
            String cityName = CityUtils.findNearestCity(world, location);
            stationName = StationUtils.createStationName(world, cityName);
        } catch (NoSuchElementException e) {
            // TODO can we do better here?
            // there are no cities, this should never happen during a proper
            // game. However, some of the unit tests create stations when there are no cities.
            stationName = "Central Station #" + world.getStations(player).size();
        }

        // check the terrain to see if we can build a station on it...
        /**
         * This {@link CompositeMove}adds a station to the station list and adds a
         * cargo bundle (to store the cargo waiting at the station) to the cargo bundle
         * list.
         */
        // TODO maybe a better way to get an id
        int id = world.getStations(player).size();
        Station station = new Station(id, location, stationName, CargoBatchBundle.EMPTY);
        station = StationUtils.calculateCargoSupplyRateAtStation(world, ruleNumber, station);

        Move addStationMove = new AddStationMove(player, station);

        CompositeMove move = new CompositeMove(Arrays.asList(upgradeTrackMove, addStationMove));

        // TODO transaction for building the station is missing (include in AddStationMove)
        // add transactions
        TrackMoveTransactionsGenerator transactionsGenerator = new TrackMoveTransactionsGenerator(world, player);
        move = transactionsGenerator.addTransactions(move);

        return move;
    }

}