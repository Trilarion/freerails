/*
 * Created on 08-Apr-2003
 *
 */
package jfreerails.world.top;

import java.util.NoSuchElementException;
import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.player.FreerailsPrincipal;


/**
 * Iterates over one of the lists on the world object only
 * returning non null elements.
 *
 * @author Luke
 *
 */
public class NonNullElements implements WorldIterator {
    private final KEY key;
    private final SKEY skey;
    private final ReadOnlyWorld w;
    private final FreerailsPrincipal principal;
    private int index = BEFORE_FIRST;
    private int row = BEFORE_FIRST;
    private int size = -1;

    public NonNullElements(SKEY k, ReadOnlyWorld world) {
        if (null == k) {
            throw new NullPointerException();
        }

        if (null == world) {
            throw new NullPointerException();
        }

        key = null;
        principal = null;
        skey = k;
        w = world;
    }

    public NonNullElements(KEY k, ReadOnlyWorld world, FreerailsPrincipal p) {
        key = k;
        w = world;
        principal = p;
        skey = null;

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

            if (nextIndex >= listSize()) {
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
        return listGet(index);
    }

    private FreerailsSerializable listGet(int i) {
        if (null == this.skey) {
            return w.get(key, i, principal);
        }
		return w.get(skey, i);
    }

    private int listSize() {
        if (null == this.skey) {
            return w.size(key, principal);
        }
		return w.size(this.skey);
    }

    public int getIndex() {
        return index;
    }

    public int getRowID() {
        return row;
    }

    public int size() {
        if (-1 == size) { //lazy loading, if we have already calculated the size don't do it again.

            int tempSize = 0;

            for (int i = 0; i < listSize(); i++) {
                if (null != listGet(i)) {
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

        for (int j = 0; j < listSize(); j++) {
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
        return null != listGet(i);
    }
	
	public int getNaturalNumber() {
		return getRowID() +1;
	}

	
	public void gotoRow(int newRow) {
		if(row == newRow){
			return;
		}
		if(row < newRow){
			while(row != newRow){
				next();
			}
		}else{
			while(row != newRow){
				previous();
			}
			
		}
		return;				
	}
	
	public static int row2index(ReadOnlyWorld w, KEY key, FreerailsPrincipal p, int row){
		int count = 0;
		for(int i = 0 ; i < w.size(key, p); i++){
			
			if(w.get(key, i, p) != null){				
				if(count == row){
					return i;
				}
				count++;
			}			
		}
		
		
		throw new NoSuchElementException(String.valueOf(row));
	}
}