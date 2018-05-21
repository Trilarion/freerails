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
package freerails.client.view;

import freerails.util.Utils;
import freerails.model.world.PlayerKey;
import freerails.model.world.NonNullElementWorldIterator;
import freerails.model.world.UnmodifiableWorld;
import freerails.model.player.Player;

import javax.swing.*;
import javax.swing.event.ListDataListener;

/**
 * Converts the interface of a list on the world object to a ListModel interface
 * that can be used by JLists. Currently, change notification is <b>not</b>
 * implemented (null elements are skipped).
 */
class WorldToListModelAdapter implements ListModel {

    private final NonNullElementWorldIterator elements;

    /**
     * @param world
     * @param playerKey
     * @param player
     */
    public WorldToListModelAdapter(UnmodifiableWorld world, PlayerKey playerKey, Player player) {
        Utils.verifyNotNull(playerKey);
        Utils.verifyNotNull(player);
        // Check that the player exists.
        if (!world.isPlayer(player)) throw new IllegalArgumentException(player.getName());

        elements = new NonNullElementWorldIterator(playerKey, world, player);
    }

    public int getSize() {
        return elements.size();
    }

    public Object getElementAt(int index) {
        elements.gotoRow(index);
        return elements.getElement();
    }

    public void addListDataListener(ListDataListener l) {
    }

    public void removeListDataListener(ListDataListener l) {
    }
}