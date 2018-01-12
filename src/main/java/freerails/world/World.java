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

import freerails.world.finances.Transaction;
import freerails.world.game.GameTime;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.player.Player;

import java.io.Serializable;

/**
 * Implements methods which can be used to alter the world. Notice
 * that in contrast to, say, {@code java.util.List} there is no remove()
 * method that shifts any subsequent elements to the left (subtracts one from
 * their indices). This means that an elements' position in a list can be used
 * as an address space independent way to reference the element. If you want to
 * remove an element from a list, you should set it to null, e.g.
 *
 *
 * {@code world.set(KEY.TRAINS, 5, null, player);}
 *
 *
 * Code that loops through lists should handle null values gracefully
 */
public interface World extends ReadOnlyWorld {

    /**
     * @param principal
     * @param element
     * @return
     */
    int addActiveEntity(FreerailsPrincipal principal, Activity element);

    /**
     * @param principal
     * @param index
     * @param element
     */
    void add(FreerailsPrincipal principal, int index, Activity element);

    /**
     * Appends the specified element to the end of the specified list and returns
     * the index that can be used to retrieve it.
     */
    int add(FreerailsPrincipal principal, KEY key, Serializable element);

    /**
     * Appends the specified element to the end of the specified list and returns
     * the index that can be used to retrieve it.
     */
    int add(SKEY key, Serializable element);

    /**
     * @param player
     * @return
     */
    int addPlayer(Player player);

    /**
     * Adds the specified transaction to the specified principal's bank account.
     */
    void addTransaction(FreerailsPrincipal p, Transaction t);

    /**
     * Returns a copy of this world object - making changes to this copy will
     * not change this object.
     */
    World defensiveCopy();

    /**
     * @param principal
     * @return
     */
    Activity removeLastActiveEntity(FreerailsPrincipal principal);

    /**
     * @param principal
     * @param index
     * @return
     */
    void removeLastActivity(FreerailsPrincipal principal, int index);

    /**
     * Removes the last element from the specified list.
     */
    Serializable removeLast(FreerailsPrincipal principal, KEY key);

    /**
     * Removes the last element from the specified list.
     */
    Serializable removeLast(SKEY key);

    /**
     * Removes and returns the last transaction added the the specified
     * principal's bank account. This method is only here so that moves that add
     * transactions can be undone.
     */
    Transaction removeLastTransaction(FreerailsPrincipal p);

    /**
     * @return
     */
    Player removeLastPlayer();

    /**
     * Replaces the element mapped to the specified item with the specified
     * element.
     */
    void set(ITEM item, Serializable element);

    /**
     * Replaces the element at the specified position in the specified list with
     * the specified element.
     */
    void set(FreerailsPrincipal principal, KEY key, int index, Serializable element);

    /**
     * Replaces the element at the specified position in the specified list with
     * the specified element.
     */

    void set(SKEY key, int index, Serializable element);

    /**
     * Replaces the tile at the specified position on the map with the specified
     * tile.
     */
    void setTile(int x, int y, Serializable tile);

    /**
     * @param t
     */
    void setTime(GameTime t);

}