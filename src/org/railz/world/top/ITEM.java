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


/** <p>This class provides a set of keys to access the items of which there can only be
 * one instance in the game world in the game world (for example, the current time).</P>
 *
 * <p>It implements the typesafe enum pattern (see Bloch, <I>Effective Java</I>
 * item 21)</p>
 */
public class ITEM implements FreerailsSerializable {
    /** Maps key numbers to KEYs */
    private static ITEM[] keys = new ITEM[getNumberOfKeys()];

    //START OF KEYS
    public static final ITEM TIME = new ITEM();
    public static final ITEM CALENDAR = new ITEM();
    public static final ITEM ECONOMY = new ITEM();
    public static final ITEM VICTORY_CONDITIONS = new ITEM();

    //END OF KEYS		
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

    int getKeyNumber() {
        return keyNumber;
    }

    private Object readResolve() throws ObjectStreamException {
        return keys[this.keyNumber];
    }
}
