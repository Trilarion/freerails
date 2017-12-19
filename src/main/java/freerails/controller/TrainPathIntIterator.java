package freerails.controller;

import freerails.util.FreerailsIntIterator;

/**
 * FlatTrackExplorer to FreerailsIntIterator adapter.
 *
 */
public class TrainPathIntIterator implements FreerailsIntIterator {
    private final FlatTrackExplorer trackExplorer;

    /**
     *
     * @param t
     */
    public TrainPathIntIterator(FlatTrackExplorer t) {
        trackExplorer = t;
    }

    /**
     *
     * @return
     */
    public boolean hasNextInt() {
        return trackExplorer.hasNextEdge();
    }

    /**
     *
     * @return
     */
    public int nextInt() {
        trackExplorer.nextEdge();
        trackExplorer.moveForward();

        return trackExplorer.getPosition();
    }
}