
package jfreerails.client.view;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.NoSuchElementException;

import jfreerails.world.common.OneTileMoveVector;

/**
 * Maps keys to OneTileMoveVectors.
 * @author Luke
 *
 */
public class KeyCode2OneTileMoveVector {
	
	  private static final HashMap<Integer,OneTileMoveVector>  keycode2vector = new HashMap<Integer,OneTileMoveVector>();
	
	static {
		 //Set up key mappings...
        //Num pad with num lock on
        keycode2vector.put(new Integer(KeyEvent.VK_NUMPAD1), OneTileMoveVector.SOUTH_WEST);
        keycode2vector.put(new Integer(KeyEvent.VK_NUMPAD2), OneTileMoveVector.SOUTH);
        keycode2vector.put(new Integer(KeyEvent.VK_NUMPAD3), OneTileMoveVector.SOUTH_EAST);
        keycode2vector.put(new Integer(KeyEvent.VK_NUMPAD4), OneTileMoveVector.WEST);
        keycode2vector.put(new Integer(KeyEvent.VK_NUMPAD6), OneTileMoveVector.EAST);
        keycode2vector.put(new Integer(KeyEvent.VK_NUMPAD7), OneTileMoveVector.NORTH_WEST);
        keycode2vector.put(new Integer(KeyEvent.VK_NUMPAD8), OneTileMoveVector.NORTH);
        keycode2vector.put(new Integer(KeyEvent.VK_NUMPAD9), OneTileMoveVector.NORTH_EAST);

        //Num pad with num lock off       
        keycode2vector.put(new Integer(KeyEvent.VK_END), OneTileMoveVector.SOUTH_WEST);
        keycode2vector.put(new Integer(KeyEvent.VK_DOWN), OneTileMoveVector.SOUTH);
        keycode2vector.put(new Integer(KeyEvent.VK_PAGE_DOWN), OneTileMoveVector.SOUTH_EAST);
        keycode2vector.put(new Integer(KeyEvent.VK_LEFT), OneTileMoveVector.WEST);
        keycode2vector.put(new Integer(KeyEvent.VK_RIGHT), OneTileMoveVector.EAST);
        keycode2vector.put(new Integer(KeyEvent.VK_HOME), OneTileMoveVector.NORTH_WEST);
        keycode2vector.put(new Integer(KeyEvent.VK_UP), OneTileMoveVector.NORTH);
        keycode2vector.put(new Integer(KeyEvent.VK_PAGE_UP), OneTileMoveVector.NORTH_EAST);
		
	}
	
	/** Returns the OneTileMoveVector that is mapped to the specified keycode.*/
    public static OneTileMoveVector getInstanceMappedToKey(int keycode)
        throws NoSuchElementException {
        Integer integer = new Integer(keycode);

        if (!keycode2vector.containsKey(integer)) {
            throw new NoSuchElementException(String.valueOf(keycode));
        }

        return keycode2vector.get(integer);
    }


}
