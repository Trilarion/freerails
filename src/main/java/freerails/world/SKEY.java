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

import freerails.util.Utils;

import java.io.Serializable;

/**
 * Provides a set of keys to access the lists of elements in the game
 * world that are shared by all players.
 *
 *
 *
 * It implements the type-safe enum pattern (see Bloch, <I>Effective Java</I>
 * item 21)
 */
@freerails.util.InstanceControlled
public class SKEY implements Serializable {
    private static final long serialVersionUID = 3257847679739506737L;

    /**
     * Maps key numbers to KEYs.
     */
    private static final SKEY[] keys = new SKEY[getNumberOfKeys()];

    // START OF KEYS

    /**
     *
     */
    public static final SKEY TERRAIN_TYPES = new SKEY();

    /**
     *
     */
    public static final SKEY WAGON_TYPES = new SKEY();

    /**
     *
     */
    public static final SKEY CARGO_TYPES = new SKEY();

    /**
     *
     */
    public static final SKEY CITIES = new SKEY();

    /**
     *
     */
    public static final SKEY ENGINE_TYPES = new SKEY();

    /**
     *
     */
    public static final SKEY TRACK_RULES = new SKEY();

    // END OF SKEYS
    private static int numberOfKeys;

    private final int keyNumber;

    private SKEY() {
        keyNumber = numberOfKeys;
        keys[keyNumber] = this;
        numberOfKeys++;
    }

    public static int getNumberOfKeys() {
        return SKEY.class.getFields().length;
    }

    public int getKeyID() {
        return keyNumber;
    }

    private Object readResolve() {
        return keys[keyNumber];
    }

    @Override
    public String toString() {
        return Utils.findConstantFieldName(this);
    }

}