package freerails.client.view;

import freerails.world.common.Step;

import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.NoSuchElementException;

/**
 * Maps keys to OneTileMoveVectors.
 *
 * @author Luke
 */
public class KeyCode2OneTileMoveVector {

    private static final HashMap<Integer, Step> keycode2vector = new HashMap<>();

    static {
        // Set up key mappings...
        // Num pad with num lock on
        keycode2vector.put(KeyEvent.VK_NUMPAD1, Step.SOUTH_WEST);
        keycode2vector.put(KeyEvent.VK_NUMPAD2, Step.SOUTH);
        keycode2vector.put(KeyEvent.VK_NUMPAD3, Step.SOUTH_EAST);
        keycode2vector.put(KeyEvent.VK_NUMPAD4, Step.WEST);
        keycode2vector.put(KeyEvent.VK_NUMPAD6, Step.EAST);
        keycode2vector.put(KeyEvent.VK_NUMPAD7, Step.NORTH_WEST);
        keycode2vector.put(KeyEvent.VK_NUMPAD8, Step.NORTH);
        keycode2vector.put(KeyEvent.VK_NUMPAD9, Step.NORTH_EAST);

        // Num pad with num lock off
        keycode2vector.put(KeyEvent.VK_END, Step.SOUTH_WEST);
        keycode2vector.put(KeyEvent.VK_DOWN, Step.SOUTH);
        keycode2vector.put(KeyEvent.VK_PAGE_DOWN, Step.SOUTH_EAST);
        keycode2vector.put(KeyEvent.VK_LEFT, Step.WEST);
        keycode2vector.put(KeyEvent.VK_RIGHT, Step.EAST);
        keycode2vector.put(KeyEvent.VK_HOME, Step.NORTH_WEST);
        keycode2vector.put(KeyEvent.VK_UP, Step.NORTH);
        keycode2vector.put(KeyEvent.VK_PAGE_UP, Step.NORTH_EAST);

    }

    /**
     * Returns the OneTileMoveVector that is mapped to the specified keycode.
     */
    public static Step getInstanceMappedToKey(int keycode)
            throws NoSuchElementException {
        Integer integer = keycode;

        if (!keycode2vector.containsKey(integer)) {
            throw new NoSuchElementException(String.valueOf(keycode));
        }

        return keycode2vector.get(integer);
    }

}
