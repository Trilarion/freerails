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

import freerails.util.Pair;
import freerails.util.Vector2D;
import freerails.model.ActivityIterator;
import freerails.model.finances.Money;
import freerails.model.finances.Transaction;
import freerails.model.game.GameTime;
import freerails.model.player.FreerailsPrincipal;
import freerails.model.player.Player;

import java.io.Serializable;

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
 * <li>A list of financial transactions for each of the players</li>
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
 *
 * Code that loops through lists should handle null values gracefully
 */
public interface ReadOnlyWorld extends Serializable {

    /**
     * @return
     */
    boolean boundsContain(Vector2D location);

    /**
     * @param principal
     * @param key
     * @param index
     * @return
     */
    boolean boundsContain(FreerailsPrincipal principal, PlayerKey key, int index);

    /**
     * @return
     */
    GameTime currentTime();

    /**
     * Returns the element mapped to the specified item.
     */

    Serializable get(WorldItem item);

    /**
     * Returns the element at the specified position in the specified list.
     */

    Serializable get(FreerailsPrincipal principal, PlayerKey key, int index);

    /**
     * Returns the element at the specified position in the specified list.
     */

    Serializable get(SharedKey key, int index);

    /**
     * @param principal
     * @param index
     * @return
     */
    ActivityIterator getActivities(FreerailsPrincipal principal, int index);

    /**
     * @param principal
     * @return
     */
    Money getCurrentBalance(FreerailsPrincipal principal);

    /**
     * @param principal
     * @return
     */
    int getID(FreerailsPrincipal principal);

    /**
     * Returns the height of the map in tiles.
     */
    int getMapHeight();

    /**
     * Returns the width of the map in tiles.
     */
    int getMapWidth();

    /**
     * @return
     */
    int getNumberOfPlayers();

    /**
     * @param principal
     * @return
     */
    int getNumberOfTransactions(FreerailsPrincipal principal);

    /**
     * @param principal
     * @return
     */
    int getNumberOfActiveEntities(FreerailsPrincipal principal);

    /**
     * @param i
     * @return
     */
    Player getPlayer(int i);

    /**
     * Returns the tile at the specified position on the map.
     */
    Serializable getTile(Vector2D location);

    /**
     * @param principal
     * @param i
     * @return
     */
    Transaction getTransaction(FreerailsPrincipal principal, int i);

    /**
     * @param principal
     * @param i
     * @return
     */
    GameTime getTransactionTimeStamp(FreerailsPrincipal principal, int i);

    /**
     * @param principal
     * @param i
     * @return
     */
    Pair<Transaction, GameTime> getTransactionAndTimeStamp(FreerailsPrincipal principal, int i);

    /**
     * @param principal
     * @return
     */
    boolean isPlayer(FreerailsPrincipal principal);

    /**
     * Returns the number of elements in the specified list.
     */
    int size(FreerailsPrincipal principal, PlayerKey key);

    /**
     * Returns the number of elements in the specified list.
     */
    int size(SharedKey key);

    /**
     * Returns number of active entities belonging to the specified principal.
     */
    int size(FreerailsPrincipal principal);
}