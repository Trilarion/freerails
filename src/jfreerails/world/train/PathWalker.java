package jfreerails.world.train;

/** This interface lets the caller retrieve a path broken into
 * a series of steps, whose length the caller specifies.
 * E.g. it could be used to get the sub section of a path that
 * a train travels during an given time inteval.
 */
public interface PathWalker {

	/** Returns true if we have not reached the end of the path.
	 */
	boolean canStepForward();

	/** Returns true if the distance forward along the path to the
	 * end exceeds the specified value.
	 * @param distance
	 * @return
	 */
    //boolean canStepForward(int distance);

	/** Moves this path walker forward to the end to the path.
	 * and returns a path iterator to retrieve the path
	 * travelled during this move.
	 */
	FreerailsPathIterator stepForward();

	/** Moves this path walker forward by the specified
	 * distance along the path and returns a path iterator
	 * to retrieve the section of the path travelled
	 * during this move.
	 */
	FreerailsPathIterator stepForward(int distance);

}
