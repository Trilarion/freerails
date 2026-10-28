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

import freerails.model.cargo.Cargo;
import freerails.model.game.*;
import freerails.model.finance.transaction.Transaction;
import freerails.model.station.Station;
import freerails.model.terrain.TerrainTile;
import freerails.model.terrain.city.City;
import freerails.model.terrain.Terrain;
import freerails.model.track.TrackType;
import freerails.model.train.Engine;
import freerails.model.train.Train;
import freerails.util.Vec2D;
import freerails.model.finance.Money;
import freerails.model.player.Player;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Collection;

/**
 * Read-oriented access to a game world's map, definitions, players, player
 * assets, finances, rules, and simulation clock. Returned collections expose
 * the world's data without providing collection-level mutation operations.
 */
public interface UnmodifiableWorld extends Serializable {

    /** Returns all engine definitions in stable identifier order. */
    Collection<Engine> getEngines();

    /**
     * @param id engine identifier
     * @return the matching engine
     */
    Engine getEngine(int id);

    /** Returns all cities in stable identifier order. */
    Collection<City> getCities();

    /**
     * @param id city identifier
     * @return the matching city
     */
    City getCity(int id);

    /** Returns all cargo definitions in their index order. */
    Collection<Cargo> getCargos();

    /**
     * @param id cargo identifier
     * @return the matching cargo definition
     */
    Cargo getCargo(int id);

    /**
     * Returns the zero-based position of a cargo in {@link #getCargos()}.
     *
     * @param cargoId stable cargo identifier
     * @return the cargo's zero-based position
     */
    int getCargoIndex(int cargoId);

    /**
     * Returns the cargo at its zero-based position in {@link #getCargos()}.
     *
     * @param index zero-based cargo position
     * @return the cargo at that position
     */
    Cargo getCargoAtIndex(int index);

    /** Returns all terrain definitions in stable identifier order. */
    Collection<Terrain> getTerrains();

    /**
     * @param id terrain identifier
     * @return the matching terrain definition
     */
    Terrain getTerrain(int id);

    /** Returns all track type definitions in their index order. */
    Collection<TrackType> getTrackTypes();

    /**
     * @param id track type identifier
     * @return the matching track type
     */
    TrackType getTrackType(int id);

    /**
     * Returns the zero-based position of a track type in {@link #getTrackTypes()}.
     *
     * @param trackTypeId stable track type identifier
     * @return the track type's zero-based position
     */
    int getTrackTypeIndex(int trackTypeId);

    /**
     * Returns the track type at its zero-based position in {@link #getTrackTypes()}.
     *
     * @param index zero-based track type position
     * @return the track type at that position
     */
    TrackType getTrackTypeAtIndex(int index);

    /**
     * Returns the trains belonging to a player in train-list order.
     *
     * @param player owner whose trains are requested
     * @return the player's trains
     */
    Collection<Train> getTrains(Player player);

    /**
     * @param player owner of the train
     * @param id train identifier
     * @return the matching train
     */
    Train getTrain(Player player, int id);

    /**
     * Returns the zero-based position of a train in the specified player's train collection.
     *
     * @param player owner of the train
     * @param trainId stable train identifier
     * @return the train's zero-based position
     */
    int getTrainIndex(Player player, int trainId);

    /**
     * Returns the stations belonging to a player in station order.
     *
     * @param player owner whose stations are requested
     * @return the player's stations
     */
    Collection<Station> getStations(Player player);

    /**
     * @param player owner of the station
     * @param id station identifier
     * @return the matching station
     */
    Station getStation(Player player, int id);

    /** Returns the sentiment currently selected by the world. */
    Sentiment getSentiment();

    /** Returns the rules currently governing this world. */
    Rules getRules();

    /**
     * Tests whether a map coordinate lies within the map boundaries.
     *
     * @param location tile coordinate to test
     * @return {@code true} if the coordinate is within the map
     */
    boolean boundsContain(Vec2D location);

    /**
     * @param player player whose current balance is requested
     * @return the player's current account balance
     */
    Money getCurrentBalance(Player player);

    /**
     * Returns the width and height of the map in tiles.
     *
     * @return a vector whose x and y values are map width and height
     */
    Vec2D getMapSize();

    /**
     * @param i zero-based position in the player list
     * @return the player at that position
     */
    Player getPlayer(int i);

    /**
     * Returns the zero-based position of the player with the specified stable ID.
     *
     * @param playerId stable player identifier
     * @return the player's zero-based position
     */
    int getPlayerIndex(int playerId);

    /** Returns the players in the order they were added. */
    Collection<Player> getPlayers();

    /**
     * Returns the tile at the specified position on the map.
     *
     * @param location map coordinate of the tile
     * @return tile at that coordinate
     */
    TerrainTile getTile(Vec2D location);

    /**
     * @param player owner of the transaction
     * @param i zero-based transaction position
     * @return the transaction at that position
     */
    Transaction getTransaction(Player player, int i);

    /**
     * Returns the transactions recorded for a player in insertion order.
     *
     * @param player account owner
     * @return the player's transaction history
     */
    Collection<Transaction> getTransactions(Player player);

    /** Returns the simulation clock. */
    Clock getClock();

    /** Returns the current simulation speed. */
    Speed getSpeed();

    /**
     * Sets the current simulation speed.
     *
     * @param speed speed to use
     */
    void setSpeed(@NotNull Speed speed);
}