package jfreerails.world.train;

import jfreerails.util.FreerailsIntIterator;
import jfreerails.world.common.FreerailsPathIterator;
import jfreerails.world.common.IntLine;
import jfreerails.world.common.PositionOnTrack;


/**
 * 30-Nov-2002
 * @author Luke Lindsay
 *
 */
public class TrainPathIterator implements FreerailsPathIterator {
    FreerailsIntIterator intIterator;
    PositionOnTrack p1 = new PositionOnTrack();
    PositionOnTrack p2 = new PositionOnTrack();
    static final int tileSize = 30;

    public TrainPathIterator(FreerailsIntIterator i) {
        intIterator = i;
        p2.setValuesFromInt(intIterator.nextInt());
    }

    public boolean hasNext() {
        return intIterator.hasNextInt();
    }

    public void nextSegment(IntLine line) {
        p1.setValuesFromInt(p2.toInt());
        line.x1 = p1.getX() * tileSize + tileSize / 2;
        line.y1 = p1.getY() * tileSize + tileSize / 2;
        p2.setValuesFromInt(intIterator.nextInt());
        line.x2 = p2.getX() * tileSize + tileSize / 2;
        line.y2 = p2.getY() * tileSize + tileSize / 2;
    }
}