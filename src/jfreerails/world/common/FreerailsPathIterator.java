package jfreerails.world.common;

/**
 * This interface lets the caller retrieve a path made up of a series of
 * straight lines. E.g. it lets the path a train takes across a section of track
 * be retrieved without revealing the underlying objects that represent the
 * track.
 * 
 * @author luke
 */
public interface FreerailsPathIterator extends FreerailsMutableSerializable {
	/**
	 * Tests whether the path has another segment.
	 */
	boolean hasNext();

	/**
	 * Gets the next segment of the path and places its coordinates in the
	 * specified IntLine; then moves the iterator forwards by one path segment.
	 * (The coordinates are placed the passed-in IntLine rather than a new
	 * object to avoid the cost of object creation.)
	 * 
	 * @param line
	 */
	void nextSegment(IntLine line);
}