package jfreerails.world.top;

import java.io.ObjectStreamException;
import jfreerails.world.common.FreerailsSerializable;


/** <p>This class provides a set of keys to access the lists of elements
 * in the game world that are shared by all players.</P>
 *
 * <p>It implements the typesafe enum pattern (see Bloch, <I>Effective Java</I>
 * item 21)</p>
 * @author Luke
 */
public class SKEY implements FreerailsSerializable {
    /** Maps key numbers to KEYs. */
    private static final SKEY[] keys = new SKEY[getNumberOfKeys()];

    //START OF KEYS
    public static final SKEY TERRAIN_TYPES = new SKEY();
    public static final SKEY WAGON_TYPES = new SKEY();
    public static final SKEY CARGO_TYPES = new SKEY();
    public static final SKEY CITIES = new SKEY();
    public static final SKEY ENGINE_TYPES = new SKEY();
    public static final SKEY TRACK_RULES = new SKEY();

    //END OF SKEYS		
    private static int numberOfKeys;
    private final int keyNumber;

    private SKEY() {
        this.keyNumber = numberOfKeys;
        keys[keyNumber] = this;
        numberOfKeys++;
    }

    static int getNumberOfKeys() {
        return SKEY.class.getFields().length;
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

    static SKEY getKey(int keyNum) {
        return keys[keyNum];
    }
}