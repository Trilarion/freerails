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
import freerails.model.track.TrackType;
import freerails.move.*;
import freerails.move.mapupdatemove.ChangeTrackPieceMove;
import freerails.util.IntPoint2D;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.player.Player;
import freerails.model.operations.station.Station;
import freerails.model.terrain.TerrainTile;
import freerails.model.track.TrackPiece;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * Generates a move that adds or upgrades a station.
 *
 * <p>The generated composite move changes the track type of the tile to the station
 * type and adds the new station. A location off the map, without connected track, or
 * with a station already is refused with a {@link RefusedMove}; everything else, such
 * as cost, overlap with other stations, terrain, and track ownership, is checked by the
 * generated move.
 *
 * <p>Cost: {@link TrackMoveTransactionsGenerator} appends the charges. Building the
 * station changes the tile's track type, so the player pays the difference in
 * purchasing price between the station type and the tile's track type for the track
 * length (nothing if the station type costs less), plus a fixed charge of the station
 * type's yearly maintenance. The charges are cash-constrained: building fails if the
 * player cannot pay them. From then on the tile's track counts as station track for
 * yearly maintenance and the balance sheet; removing the station refunds half the
 * station type's purchasing price for the removed length.
 */
public class AddStationMoveGenerator implements ClientMoveGenerator {

    @Serial
    private static final long serialVersionUID = 1L;
    private final IntPoint2D location;
    private final int ruleNumber;
    private final Player player;

    /**
     * Creates an add station move generator from the supplied values.
     *
     * @param location the location
     * @param trackRule the track rule
     * @param player the player
     */
    public AddStationMoveGenerator(IntPoint2D location, int trackRule, Player player) {
        this.location = location;
        ruleNumber = trackRule;
        this.player = player;
    }

    /**
     * Returns this generator building the station for the supplied player.
     *
     * @param player the player the generated move acts for
     * @return the rebound generator
     */
    @Override
    public @NotNull AddStationMoveGenerator withPlayer(@NotNull Player player) {
        return new AddStationMoveGenerator(location, ruleNumber, player);
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

    /**
     * @param world the game world used by this operation
     * @return the move that adds the station to the world, or a refused move if no
     *         station can be built at the location
     */
    @Override
    public Move generate(UnmodifiableWorld world) {
        if (location == null || !world.boundsContain(location)) {
            return new RefusedMove("A station must be built on the map.");
        }
        TerrainTile oldTile = world.getTile(location);
        TrackPiece oldTrackPiece = oldTile.trackPiece();
        if (oldTrackPiece != null && oldTrackPiece.trackType().isStation()) {
            return new RefusedMove("There is already a station here.");
        }
        if (oldTrackPiece == null || oldTrackPiece.trackConfiguration().get8bitTemplate() == 0) {
            return new RefusedMove("A station must be placed on existing connected track.");
        }
        TrackType trackType = world.getTrackType(ruleNumber);

        TrackPiece newTrackPiece = oldTrackPiece.withTrackType(trackType);
        Move upgradeTrackMove = new ChangeTrackPieceMove(player, oldTrackPiece, newTrackPiece, location);

        // There isn't already a station here, so assign a stable station name.
        String stationName;
        int stationId = world.allocateStationId(player);
        try {
            String cityName = CityUtils.findNearestCity(world, location);
            stationName = StationUtils.createStationName(world, cityName);
        } catch (NoSuchElementException e) {
            // There are no cities in some tests or edge cases; fall back to a deterministic name.
            stationName = "Central Station #" + stationId;
        }

        Station station = new Station(stationId, location, stationName, CargoBatchBundle.EMPTY);
        StationUtils.calculateCargoSupplyRateAtStation(world, ruleNumber, station);

        Move addStationMove = new AddStationMove(player, station);

        CompositeMove move = new CompositeMove(Arrays.asList(upgradeTrackMove, addStationMove));

        TrackMoveTransactionsGenerator transactionsGenerator = new TrackMoveTransactionsGenerator(world, player);
        move = transactionsGenerator.addTransactions(move);

        return move;
    }

}