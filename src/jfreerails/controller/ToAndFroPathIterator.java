package jfreerails.controller;

import java.util.List;
import java.util.NoSuchElementException;

import jfreerails.world.misc.FreerailsPathIterator;
import jfreerails.world.misc.FreerailsPathIteratorImpl;
import jfreerails.world.misc.IntLine;

/**
 * @author Luke Lindsay 30-Oct-2002
 *
 */
public class ToAndFroPathIterator implements FreerailsPathIterator {

	FreerailsPathIterator path;

	boolean forwards = true;

	List list;

	public ToAndFroPathIterator(List l) {
		list = l;
		nextIterator();
	}

	public void nextIterator() {
		path = new FreerailsPathIteratorImpl(list, forwards);

	}

	/**
	 * @see jfreerails.world.misc.FreerailsPathIterator#hasNext()
	 */
	public boolean hasNext() {
		if (list.size() < 2) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * @see jfreerails.world.misc.FreerailsPathIterator#nextSegment(jfreerails.world.misc.IntLine)
	 */
	public void nextSegment(IntLine line) {
		if (this.hasNext()) {
			if (!path.hasNext()) {
				forwards = !forwards;
				path = new FreerailsPathIteratorImpl(list, forwards);
			}
			path.nextSegment(line);
		} else {
			throw new NoSuchElementException();
		}
	}

}
