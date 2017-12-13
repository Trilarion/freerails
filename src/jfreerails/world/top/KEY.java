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
    private static KEY[] keys = new KEY[15];

    //START OF KEYS
    public static final KEY TRAINS = new KEY(true);
    public static final KEY CARGO_TYPES = new KEY(true);
    public static final KEY CITIES = new KEY(true);
    public static final KEY ENGINE_TYPES = new KEY(true);
    public static final KEY TRACK_RULES = new KEY(true);
    public static final KEY STATIONS = new KEY(true);
    public static final KEY TERRAIN_TYPES = new KEY(true);
    public static final KEY WAGON_TYPES = new KEY(true);
    public static final KEY BANK_ACCOUNTS = new KEY(false);

    /** The cargo waiting at stations or carried by trains. */
    public static final KEY CARGO_BUNDLES = new KEY(true);
    public static final KEY TRAIN_SCHEDULES = new KEY(true);
    public static final KEY PLAYERS = new KEY(true);

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