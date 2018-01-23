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

package freerails.world;

import freerails.util.Pair;
import freerails.util.Point2D;
import freerails.world.finances.Money;
import freerails.world.finances.Transaction;
import freerails.world.game.GameTime;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.player.Player;

import java.io.Serializable;

/**
 * This interface defines a unified set of methods to access the elements that
 * make up the game world. The game world is composed of the following
 * specific-purpose collections into which one can put game world elements.
 *
 * <ul>
 * <li>A list of players.</li>
 * <li>A 2D grid - the map.</li>
 * <li>A series of lists that are accessible using the keys defined in {@link SKEY}</li>
 * <li>Another series of lists indexed by player and accessible using the keys defined in {@link KEY}</li>
 * <li>A collection items accessible using the keys defined in {@link ITEM}</li>
 * <li>A list of financial transactions for each of the players</li>
 * </ul>
 *
 * Example: the following code gets player1's train #5.
 *
 *
 * {@code TrainModel t = (TrainModel)world.get(KEY.TRAINS, 5, player1);}
 *
 *
 * The motivation for accessing lists using keys is that one does not need to
 * add a new class or change the interface of the World class when a new list is
 * added. Instead one can just add a new entry to the class KEY.
 *
 *
 * Code that loops through lists should handle null values gracefully
 */
public interface ReadOnlyWorld extends FreerailsMutableSerializable {

    /**
     * @param x
     * @param y
     * @return
     */
    boolean boundsContain(Point2D p);

    /**
     * @param p
     * @param k
     * @param index
     * @return
     */
    boolean boundsContain(FreerailsPrincipal p, KEY k, int index);

    /**
     * @return
     */
    GameTime currentTime();

    /**
     * Returns the element mapped to the specified item.
     */

    Serializable get(ITEM item);

    /**
     * Returns the element at the specified position in the specified list.
     */

    Serializable get(FreerailsPrincipal p, KEY key, int index);

    /**
     * Returns the element at the specified position in the specified list.
     */

    Serializable get(SKEY key, int index);

    /**
     * @param p
     * @param index
     * @return
     */
    ActivityIterator getActivities(FreerailsPrincipal p, int index);

    /**
     * @param p
     * @return
     */
    Money getCurrentBalance(FreerailsPrincipal p);

    /**
     * @param p
     * @return
     */
    int getID(FreerailsPrincipal p);

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
     * @param p
     * @return
     */
    int getNumberOfTransactions(FreerailsPrincipal p);

    /**
     * @param p
     * @return
     */
    int getNumberOfActiveEntities(FreerailsPrincipal p);

    /**
     * @param i
     * @return
     */
    Player getPlayer(int i);

    /**
     * Returns the tile at the specified position on the map.
     */
    Serializable getTile(Point2D p);

    /**
     * @param p
     * @param i
     * @return
     */
    Transaction getTransaction(FreerailsPrincipal p, int i);

    /**
     * @param p
     * @param i
     * @return
     */
    GameTime getTransactionTimeStamp(FreerailsPrincipal p, int i);

    /**
     * @param p
     * @param i
     * @return
     */
    Pair<Transaction, GameTime> getTransactionAndTimeStamp(FreerailsPrincipal p, int i);

    /**
     * @param p
     * @return
     */
    boolean isPlayer(FreerailsPrincipal p);

    /**
     * Returns the number of elements in the specified list.
     */
    int size(FreerailsPrincipal p, KEY key);

    /**
     * Returns the number of elements in the specified list.
     */
    int size(SKEY key);

    /**
     * Returns number of active entities belonging to the specified principal.
     */
    int size(FreerailsPrincipal p);
}