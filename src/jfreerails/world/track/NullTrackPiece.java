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

/*
 * NullTrackPiece.java
 *
 * Created on 23 January 2002, 21:31
 */
package jfreerails.world.track;

import java.io.ObjectStreamException;


/**
 *
 * @author  lindsal
 */
final public class NullTrackPiece implements TrackPiece {
    private static final TrackPiece nullTrackPiece = new NullTrackPiece();

    /** Creates new NullTrackPiece */
    private NullTrackPiece() {
    }

    public static TrackPiece getInstance() {
        return nullTrackPiece;
    }

    public int getTrackGraphicNumber() {
        return 0;
    }

    public TrackRule getTrackRule() {
        return NullTrackType.getInstance();
    }

    public TrackConfiguration getTrackConfiguration() {
        return TrackConfiguration.getFlatInstance(0);
    }

    private Object readResolve() throws ObjectStreamException {
        return nullTrackPiece;
    }

    public boolean equals(Object o) {
        return o == this;
    }
}