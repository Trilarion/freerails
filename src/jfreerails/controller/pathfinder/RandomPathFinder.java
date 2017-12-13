/*
 * Copyright (C) 2002 Luke Lindsay
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

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