package jfreerails.world.train;

import jfreerails.world.common.FreerailsMutableSerializable;
import jfreerails.world.common.FreerailsPathIterator;

/**
 * This interface lets the caller retrieve a path broken into a series of steps,
 * whose length the caller specifies. E.g. it could be used to get the sub
 * section of a path that a train travels during an given time inteval.
 * 
 * @author Luke
 */
public interface PathWalker extends FreerailsPathIterator,
        FreerailsMutableSerializable {
    /**
     * Returns true if we have not reached the end of the path.
     */
    boolean canStepForward();

    /**
     * Moves this path walker forward by the specified distance along the path
     * and returns a path iterator to retrieve the section of the path travelled
     * during this move.
     */
    void stepForward(double distance);
}