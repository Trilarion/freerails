package jfreerails.controller.pathfinder;

import jfreerails.world.common.FreerailsPathIterator;
import jfreerails.world.common.IntLine;
import jfreerails.world.common.PositionOnTrack;


/**
 * @author Luke Lindsay 13-Oct-2002
 *
 */
public class RandomPathFinder implements FreerailsPathIterator {
    FlatTrackExplorer trackExplorer;
    PositionOnTrack p1 = new PositionOnTrack();
    PositionOnTrack p2 = new PositionOnTrack();
    static final int tileSize = 30;

    public RandomPathFinder(FlatTrackExplorer tx) {
        trackExplorer = tx;
    }

    public boolean hasNext() {
        return trackExplorer.hasNextEdge();
    }

    public void nextSegment(IntLine line) {
        p1.setValuesFromInt(trackExplorer.getPosition());
        line.x1 = p1.getX() * tileSize + tileSize / 2;
        line.y1 = p1.getY() * tileSize + tileSize / 2;
        trackExplorer.nextEdge();
        trackExplorer.moveForward();
        p2.setValuesFromInt(trackExplorer.getPosition());
        line.x2 = p2.getX() * tileSize + tileSize / 2;
        line.y2 = p2.getY() * tileSize + tileSize / 2;
    }
}