/*
 * Created on 08-Apr-2003
 *
 */
package jfreerails.world.top;

import java.util.NoSuchElementException;
import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.player.Player;


/**
 * Iterates over one of the lists on the world object only
 * returning non null elements.
 *
 * @author Luke
 *
 */
public class NonNullElements implements WorldIterator {
    private final KEY key;
    private final ReadOnlyWorld w;
    private final FreerailsPrincipal principal;
    int index = BEFORE_FIRST;
    int row = BEFORE_FIRST;
    int size = -1;

    /**
     * @deprecated in favour of NonNullElements(KEY, ReadOnlyWorld,
     * FreerailsPrincipal)
     */
    public NonNullElements(KEY k, ReadOnlyWorld world) {
        this(k, world, Player.NOBODY);
    }

    public NonNullElements(KEY k, ReadOnlyWorld world, FreerailsPrincipal p) {
        key = k;
        w = world;
        principal = p;

        if (null == k) {
            throw new NullPointerException();
        }

        if (null == world) {
            throw new NullPointerException();
        }

        if (null == p) {
            throw new NullPointerException();
        }
    }

    public boolean next() {
        int nextIndex = index; //this is used to look ahead.

        do {
            nextIndex++;

            if (nextIndex >= w.size(key, principal)) {
                return false;
            }
        } while (!testCondition(nextIndex));

        row++;
        index = nextIndex;

        return true;
    }

    public void reset() {
        index = -1;
        row = -1;
        size = -1;
    }

    public FreerailsSerializable getElement() {
        return w.get(key, index, principal);
    }

    public int getIndex() {
        return index;
    }

    public int getRowNumber() {
        return row;
    }

    public int size() {
        if (-1 == size) { //lazy loading, if we have already calculated the size don't do it again.

            int tempSize = 0;

            for (int i = 0; i < w.size(key, principal); i++) {
                if (null != w.get(key, i, principal)) {
                    tempSize++;
                }
            }

            size = tempSize;
        }

        return size;
    }

    public boolean previous() {
        int previousIndex = index; //this is used to look back.

        do {
            previousIndex--;

            if (previousIndex < 0) {
                return false;
            }
        } while (!testCondition(previousIndex));

        row--;
        index = previousIndex;

        return true;
    }

    /** Moves the cursor to the specified index.  */
    public void gotoIndex(int i) {
        int newRow = -1;

        for (int j = 0; j < w.size(key, principal); j++) {
            if (testCondition(j)) {
                newRow++;

                if (i == j) {
                    reset();
                    this.index = i;
                    this.row = newRow;

                    return;
                }
            }
        }

        throw new NoSuchElementException(String.valueOf(i));
    }

    protected boolean testCondition(int i) {
        return null != w.get(key, i, principal);
    }
}