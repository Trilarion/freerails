package freerails.controller;

import freerails.util.FreerailsIntIterator;

/**
 * FlatTrackExplorer to FreerailsIntIterator adapter.
 *
 * @author Luke Lindsay 30-Nov-2002.
 */
public class TrainPathIntIterator implements FreerailsIntIterator {
    private final FlatTrackExplorer trackExplorer;

    public TrainPathIntIterator(FlatTrackExplorer t) {
        trackExplorer = t;
    }

    public boolean hasNextInt() {
        return trackExplorer.hasNextEdge();
    }

    public int nextInt() {
        trackExplorer.nextEdge();
        trackExplorer.moveForward();

        return trackExplorer.getPosition();
    }
}