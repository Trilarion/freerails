package freerails.controller;

import freerails.util.FreerailsIntIterator;

/**
 * FlatTrackExplorer to FreerailsIntIterator adapter.
 *
 * @author Luke Lindsay 30-Nov-2002.
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