/*
 * Created on 08-Apr-2003
 *
 */
package jfreerails.world.top;

import jfreerails.world.common.FreerailsSerializable;

/**
 * Iterates over one of the lists on the world object only
 * returning non null elements.
 *
 * @author Luke
 *
 */
public class NonNullElements implements WorldIterator {
    
    private final KEY key;
    
    private final World w;
    
    int index = BEFORE_FIRST;
    
    int row = BEFORE_FIRST;
    
    int size = -1;
    
    public NonNullElements(KEY k, World world) {
        key = k;
        w = world;
    }
    
    public boolean next() {
        int nextIndex = index; //this is used to look ahead.
        do {
            nextIndex++;
            if (nextIndex >= w.size(key)) {
                return false;
            }
        } while (null == w.get(key, nextIndex));
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
        return w.get(key, index);
    }
    
    public int getIndex() {
        return index;
    }
    
    public int getRowNumber() {
        return row;
    }
    
    public int size() {
        if(-1 == size){ //lazy loading, if we have already calculated the size don't do it again.
            int tempSize=0;
            for(int i = 0; i < w.size(key) ; i ++){
                if(null!=w.get(key, i)){
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
        } while (null == w.get(key, previousIndex));
        row--;
        index = previousIndex;
        return true;
    }
    
}
