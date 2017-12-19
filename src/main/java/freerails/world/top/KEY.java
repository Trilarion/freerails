package freerails.world.top;

import freerails.util.Utils;
import freerails.world.FreerailsSerializable;

import java.io.ObjectStreamException;

/**
 *
 * This class provides a set of keys to access the lists of elements in the game
 * world that are indexed by player.
 *
 *
 *
 * It implements the typesafe enum pattern (see Bloch, <I>Effective Java</I>
 * item 21)
 *
 *
 */
@freerails.util.InstanceControlled
public class KEY implements FreerailsSerializable {
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
        this.keyNumber = numberOfKeys;
        keys[keyNumber] = this;
        numberOfKeys++;
    }

    static int getNumberOfKeys() {
        return numberOfKeys;
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

    /**
     *
     * @param keyNum
     * @return
     */
    public static KEY getKey(int keyNum) {
        return keys[keyNum];
    }
}