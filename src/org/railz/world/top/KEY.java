/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.railz.world.top;

import java.io.ObjectStreamException;
import org.railz.world.common.FreerailsSerializable;


/** <p>This class provides a set of keys to access the lists of elements
 * in the game world.</P>
 *
 * <p>It implements the typesafe enum pattern (see Bloch, <I>Effective Java</I>
 * item 21)</p>
 */
public class KEY implements FreerailsSerializable {
    /** Maps key numbers to KEYs */
    private static KEY[] keys = new KEY[15];

    //START OF KEYS
    public static final KEY TRAINS = new KEY(false);
    public static final KEY CARGO_TYPES = new KEY(true);
    public static final KEY CITIES = new KEY(true);
    public static final KEY ENGINE_TYPES = new KEY(true);
    public static final KEY TRACK_RULES = new KEY(true);
    public static final KEY STATIONS = new KEY(false);
    public static final KEY TERRAIN_TYPES = new KEY(true);
    public static final KEY WAGON_TYPES = new KEY(true);
    public static final KEY BANK_ACCOUNTS = new KEY(false);
    public static final KEY BALANCE_SHEETS = new KEY(false);

    /** The cargo waiting at stations or carried by trains. */
    public static final KEY CARGO_BUNDLES = new KEY(true);
    public static final KEY TRAIN_SCHEDULES = new KEY(false);
    public static final KEY PLAYERS = new KEY(true);
    public static final KEY BUILDING_TYPES = new KEY(true);
    public static final KEY STATISTICS = new KEY(false);

    //END OF KEYS		
    private static int numberOfKeys;
    private final int keyNumber;

    /**
     * Whether ownership of the objects is common to all Principals in the
     * game.
     */
    public final boolean shared;

    /**
     * @param shared Whether the objects are common to all Principals in the
     * game
     */
    private KEY(boolean shared) {
        this.keyNumber = numberOfKeys;
        this.shared = shared;
        keys[keyNumber] = this;
        numberOfKeys++;
    }

    static int getNumberOfKeys() {
        return numberOfKeys;
    }

    int getKeyNumber() {
        return keyNumber;
    }

    private Object readResolve() throws ObjectStreamException {
        return keys[this.keyNumber];
    }

    public String toString() {
        return String.valueOf(getKeyNumber());
    }

    static KEY getKey(int keyNum) {
        return keys[keyNum];
    }
}
