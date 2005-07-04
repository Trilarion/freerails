package jfreerails.world.top;

import java.io.ObjectStreamException;

import jfreerails.util.Utils;
import jfreerails.world.common.FreerailsSerializable;

/**
 * <p>
 * This class provides a set of keys to access things that change over time, for
 * example train positions.
 * </P>
 * 
 * @author Luke
 */
@jfreerails.util.InstanceControlled
public class AKEY implements FreerailsSerializable {
	private static final long serialVersionUID = 3257847679739506737L;

	/** Maps key numbers to KEYs. */
	private static final AKEY[] keys = new AKEY[getNumberOfKeys()];

	// START OF KEYS
	public static final AKEY TRAIN_POSITIONS = new AKEY();

	// END OF SKEYS
	private static int numberOfKeys;

	private final int keyNumber;

	private AKEY() {
		this.keyNumber = numberOfKeys;
		keys[keyNumber] = this;
		numberOfKeys++;
	}

	static int getNumberOfKeys() {
		return AKEY.class.getFields().length;
	}

	int getKeyID() {
		return keyNumber;
	}

	private Object readResolve() throws ObjectStreamException {
		return keys[this.keyNumber];
	}

	public String toString() {
		return Utils.findConstantFieldName(this);
	}

	static AKEY getKey(int keyNum) {
		return keys[keyNum];
	}
}