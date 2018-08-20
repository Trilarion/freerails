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

package freerails.model.world;

import freerails.model.cargo.Cargo;
import freerails.model.game.*;
import freerails.model.finance.transaction.Transaction;
import freerails.model.station.Station;
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
 * This interface defines a unified set of methods to access the elements that
 * make up the game world. The game world is composed of the following
 * specific-purpose collections into which one can put game world elements.
 *
 * <ul>
 * <li>A list of players.</li>
 * <li>A 2D grid - the map.</li>
 * <li>A series of lists that are accessible using the keys defined in {@link SharedKey}</li>
 * <li>Another series of lists indexed by player and accessible using the keys defined in {@link PlayerKey}</li>
 * <li>A collection items accessible using the keys defined in {@link WorldItem}</li>
 * <li>A list of financial transaction for each of the players</li>
 * </ul>
 *
 * Example: the following code gets player1's train #5.
 *
 *
 * {@code TrainModel t = (TrainModel)world.get(KEY.Trains, 5, player1);}
 *
 *
 * The motivation for accessing lists using keys is that one does not need to
 * add a new class or change the interface of the World class when a new list is
 * added. Instead one can just add a new entry to the class KEY.
 *
 * Code that loops through lists should handle null values gracefully.
 */
public interface UnmodifiableWorld extends Serializable {

    Collection<Engine> getEngines();

    Engine getEngine(int id);

    Collection<City> getCities();

    City getCity(int id);

    Collection<Cargo> getCargos();

    Cargo getCargo(int id);

    Collection<Terrain> getTerrains();

    Terrain getTerrain(int id);

    Collection<TrackType> getTrackTypes();

    TrackType getTrackType(int id);

    Collection<Train> getTrains(Player player);

    Train getTrain(Player player, int id);

    Collection<Station> getStations(Player player);

    Station getStation(Player player, int id);

    Sentiment getSentiment();

    Rules getRules();

    /**
     * @return
     */
    boolean boundsContain(Vec2D location);

    /**
     * @param player
     * @return
     */
    Money getCurrentBalance(Player player);

    /**
     * Returns the width and height of the map in tiles.
     *
     * @return
     */
    Vec2D getMapSize();

    /**
     * @param i
     * @return
     */
    Player getPlayer(int i);

    Collection<Player> getPlayers();

    /**
     * Returns the tile at the specified position on the map.
     */
    Serializable getTile(Vec2D location);

    /**
     * @param player
     * @param i
     * @return
     */
    Transaction getTransaction(Player player, int i);

    Collection<Transaction> getTransactions(Player player);

    Clock getClock();

    Speed getSpeed();

    void setSpeed(@NotNull Speed speed);
}