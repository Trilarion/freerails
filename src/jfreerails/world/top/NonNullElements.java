/*
 * Created on 08-Apr-2003
 * 
 */
package jfreerails.world.top;

import jfreerails.world.common.FreerailsSerializable;

/**
 * Iterates over the elements of one of the lists on the world object only
 * returning non null elements.
 * 
 * @author Luke
 * 
 */
public class NonNullElements implements WorldIterator {

	private final KEY key;

	private final World w;

	int index = -1;

	int row = -1;

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
		} while (null != w.get(key, nextIndex));
		row++;
		index = nextIndex;
		return true;
	}

	public void beforeFirst() {
		index = -1;
		row = -1;
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

}
