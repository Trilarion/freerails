package jfreerails.world.top;

import java.io.ObjectStreamException;
import jfreerails.world.common.FreerailsSerializable;


/** <p>This class provides a set of keys to access the lists of elements
 * in the game world.</P>
 *
 * <p>It implements the typesafe enum pattern (see Bloch, <I>Effective Java</I>
 * item 21)</p>
 */
public class KEY implements FreerailsSerializable {
    /** Maps key numbers to KEYs */
    private static KEY[] keys = new KEY[getNumberOfKeys()];

    //START OF KEYS
    public static final KEY TRAINS = new KEY();
    public static final KEY BONDS = new KEY();
    public static final KEY CARGO_TYPES = new KEY();
    public static final KEY CITIES = new KEY();
    public static final KEY COMPANIES = new KEY();
    public static final KEY ENGINE_TYPES = new KEY();
    public static final KEY INDUSTRY_TYPES = new KEY();
    public static final KEY TRACK_RULES = new KEY();
    public static final KEY STATIONS = new KEY();
    public static final KEY TERRAIN_TYPES = new KEY();
    public static final KEY WAGON_TYPES = new KEY();
    public static final KEY BANK_ACCOUNTS = new KEY();

    /** The cargo waiting at stations or carried by trains. */
    public static final KEY CARGO_BUNDLES = new KEY();
    public static final KEY TRAIN_SCHEDULES = new KEY();

    //END OF KEYS		
    private static int numberOfKeys = 0;
    private final int keyNumber;

    private KEY() {
        this.keyNumber = numberOfKeys;
        keys[keyNumber] = this;
        numberOfKeys++;
    }

    static int getNumberOfKeys() {
        return KEY.class.getFields().length;
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
}