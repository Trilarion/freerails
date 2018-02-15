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

import freerails.util.Utils;

import java.io.Serializable;

// TODO what about enums instead?
/**
 * Provides a set of items to access the items of which there can only
 * be one instance in the game world (for example, the current time).
 *
 * It implements the type-safe enum pattern (see Bloch, <I>Effective Java</I> item 21)
 */
public class ITEM implements Serializable {

    private static final long serialVersionUID = 3257846593180151859L;
    /**
     * Maps key numbers to KEYs.
     */
    private static final ITEM[] items = new ITEM[getNumberOfKeys()];

    public static final ITEM CALENDAR = new ITEM();
    public static final ITEM GAME_RULES = new ITEM();
    public static final ITEM GAME_SPEED = new ITEM();
    public static final ITEM ECONOMIC_CLIMATE = new ITEM();
    private static int numberOfKeys = 0;
    private final int keyID;

    private ITEM() {
        keyID = numberOfKeys;
        items[keyID] = this;
        numberOfKeys++;
    }

    public static int getNumberOfKeys() {
        return ITEM.class.getFields().length;
    }

    public int getKeyID() {
        return keyID;
    }

    protected Object readResolve() {
        return items[keyID];
    }

    @Override
    public String toString() {
        return Utils.findConstantFieldName(this);
    }
}