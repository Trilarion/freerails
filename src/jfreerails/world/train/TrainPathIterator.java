/*
 * Copyright (C) Luke Lindsay
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