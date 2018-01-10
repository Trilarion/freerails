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

// TODO how is this different from ITEM?
/**
 * Provides a set of keys to access the lists of elements in the game
 * world that are indexed by player.
 *
 * It implements the type-safe enum pattern (see Bloch, <I>Effective Java</I>
 * item 21)
 */
@freerails.util.InstanceControlled
public class KEY implements Serializable {
    private static final long serialVersionUID = 3257572793275987001L;

    /**
     * Maps key numbers to KEYs.
     */
    private static final KEY[] keys = new KEY[15];

    // START OF KEYS

    /**
     *
     */
    public static final KEY TRAINS = new KEY();

    // public static final KEY TRAIN_POSITIONS = new KEY();

    /**
     *
     */

    public static final KEY STATIONS = new KEY();

    /**
     * The cargo waiting at stations or carried by trains.
     */
    public static final KEY CARGO_BUNDLES = new KEY();

    /**
     *
     */
    public static final KEY TRAIN_SCHEDULES = new KEY();

    // END OF KEYS
    private static int numberOfKeys;

    private final int keyNumber;

    private KEY() {
        keyNumber = numberOfKeys;
        keys[keyNumber] = this;
        numberOfKeys++;
    }

    public static int getNumberOfKeys() {
        return numberOfKeys;
    }

    int getKeyID() {
        return keyNumber;
    }

    private Object readResolve() {
        return keys[keyNumber];
    }

    @Override
    public String toString() {
        return Utils.findConstantFieldName(this);
    }

    /**
     * @param keyNum
     * @return
     */
    public static KEY getKey(int keyNum) {
        return keys[keyNum];
    }
}