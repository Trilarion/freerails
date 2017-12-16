package freerails.world.top;

import freerails.util.Utils;
import freerails.world.common.FreerailsSerializable;

import java.io.ObjectStreamException;

/**
 * <p>
 * This class provides a set of keys to access the items of which there can only
 * be one instance in the game world in the game world (for example, the current
 * time).
 * </P>
 * <p>
 * <p>
 * It implements the typesafe enum pattern (see Bloch, <I>Effective Java</I>
 * item 21)
 * </p>
 *
 * @author Luke
 */
@freerails.util.InstanceControlled
public class ITEM implements FreerailsSerializable {
    private static final long serialVersionUID = 3257846593180151859L;

    /**
     * Maps key numbers to KEYs.
     */
    private static final ITEM[] keys = new ITEM[getNumberOfKeys()];

    // START OF KEYS
    public static final ITEM CALENDAR = new ITEM();

    public static final ITEM GAME_RULES = new ITEM();

    public static final ITEM GAME_SPEED = new ITEM();

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