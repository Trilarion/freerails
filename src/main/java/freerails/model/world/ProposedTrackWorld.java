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

package freerails.model.world;

import freerails.model.building.EngineShop;
import freerails.model.economy.Money;
import freerails.model.economy.transaction.Transaction;
import freerails.model.game.Rules;
import freerails.model.game.Sentiment;
import freerails.model.operations.cargo.Cargo;
import freerails.model.operations.station.Station;
import freerails.model.operations.train.Engine;
import freerails.model.operations.train.Train;
import freerails.model.player.Player;
import freerails.model.simulation.Clock;
import freerails.model.simulation.Speed;
import freerails.model.terrain.Terrain;
import freerails.model.terrain.TerrainTile;
import freerails.model.terrain.city.City;
import freerails.model.track.TrackPiece;
import freerails.model.track.TrackType;
import freerails.util.IntPoint2D;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A read-only view of a world with proposed track changes on top, used to preview
 * track building without copying the world.
 *
 * <p>For a tile with a proposed track piece, {@link #getTile} returns the live
 * tile's terrain with the proposed piece; a proposed {@code null} piece means the
 * track there is removed. Everything else, including stations, trains, and
 * transactions, is read from the live world, so only track differs from it. The
 * view changes no state: {@link #setSpeed} is not supported.
 */
public final class ProposedTrackWorld implements UnmodifiableWorld {

    @Serial
    private static final long serialVersionUID = 1L;

    private final UnmodifiableWorld world;
    private final Map<IntPoint2D, TrackPiece> proposedTrack = new LinkedHashMap<>();
    private final Map<IntPoint2D, TrackPiece> proposedTrackView = Collections.unmodifiableMap(proposedTrack);

    /**
     * Creates a view of the supplied world without proposed track.
     *
     * @param world the live world
     */
    public ProposedTrackWorld(@NotNull UnmodifiableWorld world) {
        this.world = Objects.requireNonNull(world);
    }

    /**
     * Proposes the track piece of a tile.
     *
     * @param location the tile
     * @param trackPiece the proposed track piece, or {@code null} to remove the track
     * @throws IllegalArgumentException if the tile is not on the map
     */
    public void propose(@NotNull IntPoint2D location, TrackPiece trackPiece) {
        if (!world.boundsContain(location)) {
            throw new IllegalArgumentException("Proposed track must be on the map: " + location);
        }
        proposedTrack.put(location, trackPiece);
    }

    /**
     * Drops all proposed track.
     */
    public void clear() {
        proposedTrack.clear();
    }

    /**
     * Returns the proposed track pieces by tile, in the order they were proposed. A
     * {@code null} piece means the track there is removed. The map is a live,
     * unmodifiable view; it is not synchronized, so read it under the same lock as
     * the changes, or copy it.
     *
     * @return the proposed track pieces
     */
    public Map<IntPoint2D, TrackPiece> getProposedTrack() {
        return proposedTrackView;
    }

    /**
     * Returns the tile with its proposed track piece, if there is one.
     *
     * @param location the tile
     * @return the tile as proposed
     */
    @Override
    public TerrainTile getTile(IntPoint2D location) {
        TerrainTile liveTile = world.getTile(location);
        if (!proposedTrack.containsKey(location)) {
            return liveTile;
        }
        return new TerrainTile(liveTile.terrainTypeId(), proposedTrack.get(location));
    }

    /**
     * Not supported: the view changes no state.
     *
     * @param speed ignored
     * @throws UnsupportedOperationException always
     */
    @Override
    public void setSpeed(@NotNull Speed speed) {
        throw new UnsupportedOperationException("A proposed-track view cannot change the game speed.");
    }

    // Everything else is read from the live world.

    @Override
    public Collection<Engine> getEngines() {
        return world.getEngines();
    }

    @Override
    public Engine getEngine(int id) {
        return world.getEngine(id);
    }

    @Override
    public Collection<City> getCities() {
        return world.getCities();
    }

    @Override
    public City getCity(int id) {
        return world.getCity(id);
    }

    @Override
    public Collection<Cargo> getCargos() {
        return world.getCargos();
    }

    @Override
    public Cargo getCargo(int id) {
        return world.getCargo(id);
    }

    @Override
    public int getCargoIndex(int cargoId) {
        return world.getCargoIndex(cargoId);
    }

    @Override
    public Cargo getCargoAtIndex(int index) {
        return world.getCargoAtIndex(index);
    }

    @Override
    public Collection<Terrain> getTerrains() {
        return world.getTerrains();
    }

    @Override
    public Terrain getTerrain(int id) {
        return world.getTerrain(id);
    }

    @Override
    public Collection<TrackType> getTrackTypes() {
        return world.getTrackTypes();
    }

    @Override
    public TrackType getTrackType(int id) {
        return world.getTrackType(id);
    }

    @Override
    public int getTrackTypeIndex(int trackTypeId) {
        return world.getTrackTypeIndex(trackTypeId);
    }

    @Override
    public TrackType getTrackTypeAtIndex(int index) {
        return world.getTrackTypeAtIndex(index);
    }

    @Override
    public Collection<Train> getTrains(Player player) {
        return world.getTrains(player);
    }

    @Override
    public Train getTrain(Player player, int id) {
        return world.getTrain(player, id);
    }

    @Override
    public int getTrainIndex(Player player, int trainId) {
        return world.getTrainIndex(player, trainId);
    }

    @Override
    public int allocateTrainId(Player player) {
        return world.allocateTrainId(player);
    }

    @Override
    public int allocateStationId(Player player) {
        return world.allocateStationId(player);
    }

    @Override
    public Collection<Station> getStations(Player player) {
        return world.getStations(player);
    }

    @Override
    public Station getStation(Player player, int id) {
        return world.getStation(player, id);
    }

    @Override
    public Collection<EngineShop> getEngineShops(Player player) {
        return world.getEngineShops(player);
    }

    @Override
    public EngineShop getEngineShop(Player player, int id) {
        return world.getEngineShop(player, id);
    }

    @Override
    public Sentiment getSentiment() {
        return world.getSentiment();
    }

    @Override
    public Collection<Sentiment> getSentiments() {
        return world.getSentiments();
    }

    @Override
    public Rules getRules() {
        return world.getRules();
    }

    @Override
    public boolean boundsContain(IntPoint2D location) {
        return world.boundsContain(location);
    }

    @Override
    public Money getCurrentBalance(Player player) {
        return world.getCurrentBalance(player);
    }

    @Override
    public IntPoint2D getMapSize() {
        return world.getMapSize();
    }

    @Override
    public Player getPlayer(int i) {
        return world.getPlayer(i);
    }

    @Override
    public int getPlayerIndex(int playerId) {
        return world.getPlayerIndex(playerId);
    }

    @Override
    public Collection<Player> getPlayers() {
        return world.getPlayers();
    }

    @Override
    public Transaction getTransaction(Player player, int i) {
        return world.getTransaction(player, i);
    }

    @Override
    public Collection<Transaction> getTransactions(Player player) {
        return world.getTransactions(player);
    }

    @Override
    public Clock getClock() {
        return world.getClock();
    }

    @Override
    public boolean isCityGrowthEnabled() {
        return world.isCityGrowthEnabled();
    }

    @Override
    public Speed getSpeed() {
        return world.getSpeed();
    }
}
