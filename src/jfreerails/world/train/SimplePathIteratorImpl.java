package jfreerails.world.train;

import java.util.NoSuchElementException;

import jfreerails.world.misc.FreerailsPathIterator;
import jfreerails.world.misc.IntLine;

public class SimplePathIteratorImpl implements FreerailsPathIterator {

	boolean hasNext = true;

	int[] x;
	int[] y;

	int position = 0;

	public SimplePathIteratorImpl(int[] xpoints, int[] ypoints) {
		x = (int[]) xpoints.clone();
		y = (int[]) ypoints.clone(); //defensive copy.
		if (x.length != y.length) {
			throw new IllegalArgumentException("The array length of the array must be even");
		}
	}

	public void nextSegment(IntLine line) {
		if (hasNext()) {
			line.x1 = x[position];
			line.y1 = y[position];
			line.x2 = x[position + 1];
			line.y2 = y[position + 1];
			position++;
		} else {
			throw new NoSuchElementException();
		}
	}

	public boolean hasNext() {
		return (position + 1) < x.length;
	}
	
}