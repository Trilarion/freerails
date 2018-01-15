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

package freerails.world.train;

import freerails.world.FreerailsMutableSerializable;
import freerails.world.terrain.TileTransition;

/**
 * A <b>mutable</b> class that stores the coordinates of the tile on entity is
 * standing on and the direction in which the entity is facing (usually the
 * direction the entity as just been moving - the opposite to the direction it
 * came from), it provides methods to encode and decode its field values to and
 * from a single int.
 */
public final class PositionOnTrack implements FreerailsMutableSerializable {

    public static final int BITS_FOR_COORDINATE = 14;
    private static final int BITS_FOR_DIRECTION = 3;
    public static final int MAX_COORDINATE = (1 << BITS_FOR_COORDINATE) - 1;
    public static final int MAX_DIRECTION = (1 << BITS_FOR_DIRECTION) - 1;
    private static final long serialVersionUID = 3257853198755707184L;
    /**
     * The direction from which we entered the tile.
     */
    private TileTransition cameFrom = TileTransition.NORTH;
    private int x = 0;
    private int y = 0;

    /**
     *
     */
    public PositionOnTrack() {
    }

    /**
     * @param i
     */
    public PositionOnTrack(int i) {
        setValuesFromInt(i);
    }

    private PositionOnTrack(int x, int y, TileTransition direction) {
        if (x > MAX_COORDINATE || x < 0) {
            throw new IllegalArgumentException("x=" + x);
        }

        if (y > MAX_COORDINATE || y < 0) {
            throw new IllegalArgumentException("y=" + y);
        }

        this.x = x;
        this.y = y;

        cameFrom = direction;
    }

    /**
     * @param x
     * @param y
     * @param direction
     * @return
     */
    public static PositionOnTrack createComingFrom(int x, int y, TileTransition direction) {
        return new PositionOnTrack(x, y, direction);
    }

    /**
     * @param x
     * @param y
     * @param direction
     * @return
     */
    public static PositionOnTrack createFacing(int x, int y, TileTransition direction) {
        return new PositionOnTrack(x, y, direction.getOpposite());
    }

    /**
     * @param pos
     * @return
     */
    public static int[] toInts(PositionOnTrack[] pos) {
        int[] returnValue = new int[pos.length];
        for (int i = 0; i < pos.length; i++) {
            returnValue[i] = pos[i].toInt();
        }
        return returnValue;
    }

    /**
     * @param x
     * @param y
     * @return
     */
    public static int toInt(int x, int y) {
        return x | (y << BITS_FOR_COORDINATE);
    }

    /**
     * @return The direction the entity came from.
     */
    public TileTransition cameFrom() {
        return cameFrom;
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }

        if (obj instanceof PositionOnTrack) {
            PositionOnTrack other = (PositionOnTrack) obj;

            return other.cameFrom() == cameFrom() && other.x == x && other.y == y;
        }
        return false;
    }

    /**
     * @return The direction the entity is facing.
     */
    public TileTransition facing() {
        return cameFrom.getOpposite();
    }

    /**
     * @return the position on the track which is in the opposite direction.
     */
    public PositionOnTrack getOpposite() {
        int newX = x - cameFrom.deltaX;
        int newY = y - cameFrom.deltaY;
        TileTransition newDirection = cameFrom.getOpposite();

        return createComingFrom(newX, newY, newDirection);
    }

    /**
     * @return
     */
    public int getX() {
        return x;
    }

    /**
     * @param x
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * @return
     */
    public int getY() {
        return y;
    }

    /**
     * @param y
     */
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public int hashCode() {
        int result;
        result = x;
        result = 29 * result + y;
        result = 29 * result + cameFrom.hashCode();

        return result;
    }

    /**
     * @param v
     */
    public void setCameFrom(TileTransition v) {
        cameFrom = v;
    }

    /**
     * @param i
     */
    public void setValuesFromInt(int i) {
        x = i & MAX_COORDINATE;

        int shiftedY = i & (MAX_COORDINATE << BITS_FOR_COORDINATE);
        y = shiftedY >> BITS_FOR_COORDINATE;

        int shiftedDirection = i & (MAX_DIRECTION << (2 * BITS_FOR_COORDINATE));
        int directionAsInt = shiftedDirection >> (2 * BITS_FOR_COORDINATE);
        cameFrom = TileTransition.getInstance(directionAsInt);
    }

    /**
     * @param tileTransition
     */
    public void move(TileTransition tileTransition) {
        x += tileTransition.deltaX;
        y += tileTransition.deltaY;
        cameFrom = tileTransition.getOpposite();
    }

    /**
     * @return an integer representing this PositionOnTrack object
     */
    public int toInt() {
        int i = x | (y << BITS_FOR_COORDINATE);

        int directionAsInt = cameFrom.getID();
        int shiftedDirection = (directionAsInt << (2 * BITS_FOR_COORDINATE));
        i = i | shiftedDirection;

        return i;
    }

    @Override
    public String toString() {

        return "PositionOnTrack: " + x + ", " + y + " facing " + cameFrom.getOpposite().toString();
    }
}