/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package freerails.model.train;

import freerails.util.Vec2D;
import freerails.model.terrain.TileTransition;

/**
 * A <b>mutable</b> class that stores the coordinates of the tile an entity is
 * standing on and the direction in which the entity is facing (usually the
 * direction the entity as just been moving - the opposite to the direction it
 * came from), it provides methods to encode and decode its field values to and
 * from a single int.
 */
public class PositionOnTrack {

    public static final int BITS_FOR_COORDINATE = 14;
    private static final int BITS_FOR_DIRECTION = 3;
    public static final int MAX_COORDINATE = (1 << BITS_FOR_COORDINATE) - 1;
    public static final int MAX_DIRECTION = (1 << BITS_FOR_DIRECTION) - 1;
    private static final long serialVersionUID = 3257853198755707184L;
    /**
     * The direction from which we entered the tile.
     */
    private TileTransition comingFrom = TileTransition.NORTH;
    private Vec2D location = Vec2D.ZERO;

    /**
     *
     */
    public PositionOnTrack() {}

    /**
     * @param i
     */
    public PositionOnTrack(int i) {
        setValuesFromInt(i);
    }

    public PositionOnTrack(Vec2D location, TileTransition comingFrom) {
        if (location.x > MAX_COORDINATE || location.x < 0) {
            throw new IllegalArgumentException("x=" + location.x);
        }

        if (location.y > MAX_COORDINATE || location.y < 0) {
            throw new IllegalArgumentException("y=" + location.y);
        }

        this.location = location;
        this.comingFrom = comingFrom;
    }

    /**
     * @return The direction the entity came from.
     */
    public TileTransition getComingFrom() {
        return comingFrom;
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }

        if (obj instanceof PositionOnTrack) {
            PositionOnTrack other = (PositionOnTrack) obj;

            return other.getComingFrom() == getComingFrom() && location.equals(other.location);
        }
        return false;
    }

    /**
     * @return The direction the entity is facing.
     */
    public TileTransition getFacingTo() {
        return comingFrom.getOpposite();
    }

    public void setLocation(Vec2D location) {
        this.location = location;
    }

    public Vec2D getLocation() {
        return location;
    }

    @Override
    public int hashCode() {
        int result;
        result = location.hashCode();
        result = 29 * result + comingFrom.hashCode();

        return result;
    }

    /**
     * @param v
     */
    public void setComingFrom(TileTransition v) {
        comingFrom = v;
    }

    /**
     * @param i
     */
    public void setValuesFromInt(int i) {
        int x = i & MAX_COORDINATE;

        int shiftedY = i & (MAX_COORDINATE << BITS_FOR_COORDINATE);
        int y = shiftedY >> BITS_FOR_COORDINATE;
        location = new Vec2D(x, y);

        int shiftedDirection = i & (MAX_DIRECTION << (2 * BITS_FOR_COORDINATE));
        int directionAsInt = shiftedDirection >> (2 * BITS_FOR_COORDINATE);
        comingFrom = TileTransition.getInstance(directionAsInt);
    }

    /**
     * @param tileTransition
     */
    public void move(TileTransition tileTransition) {
        location = new Vec2D(location.x + tileTransition.deltaX, location.y + tileTransition.deltaY);
        comingFrom = tileTransition.getOpposite();
    }

    /**
     * @return an integer representing this PositionOnTrack object
     */
    public int toInt() {
        int i = location.x | (location.y << BITS_FOR_COORDINATE);

        int directionAsInt = comingFrom.getID();
        int shiftedDirection = (directionAsInt << (2 * BITS_FOR_COORDINATE));
        i = i | shiftedDirection;

        return i;
    }

    @Override
    public String toString() {
        return "PositionOnTrack: " + location + " facing " + comingFrom.getOpposite().toString();
    }
}