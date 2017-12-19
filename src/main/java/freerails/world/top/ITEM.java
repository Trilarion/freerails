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

package freerails.world.top;

import freerails.util.Utils;
import freerails.world.FreerailsSerializable;

import java.io.ObjectStreamException;

/**
 *
 * This class provides a set of keys to access the items of which there can only
 * be one instance in the game world in the game world (for example, the current
 * time).
 *
 *
 *
 * It implements the typesafe enum pattern (see Bloch, <I>Effective Java</I>
 * item 21)
 *
 *
 */
@freerails.util.InstanceControlled
public class ITEM implements FreerailsSerializable {
    private static final long serialVersionUID = 3257846593180151859L;

    /**
     * Maps key numbers to KEYs.
     */
    private static final ITEM[] keys = new ITEM[getNumberOfKeys()];

    // START OF KEYS

    /**
     *
     */
    public static final ITEM CALENDAR = new ITEM();

    /**
     *
     */
    public static final ITEM GAME_RULES = new ITEM();

    /**
     *
     */
    public static final ITEM GAME_SPEED = new ITEM();

    /**
     *
     */
    public static final ITEM ECONOMIC_CLIMATE = new ITEM();

    // END OF KEYS
    private static int numberOfKeys = 0;

    private final int keyNumber;

    private ITEM() {
        this.keyNumber = numberOfKeys;
        keys[keyNumber] = this;
        numberOfKeys++;
    }

    static int getNumberOfKeys() {
        return ITEM.class.getFields().length;
    }

    int getKeyID() {
        return keyNumber;
    }

    private Object readResolve() throws ObjectStreamException {
        return keys[this.keyNumber];
    }

    @Override
    public String toString() {
        return Utils.findConstantFieldName(this);
    }
}