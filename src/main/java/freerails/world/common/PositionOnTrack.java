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

package freerails.world.common;

/**
 * A <b>mutable</b> class that stores the coordinates of the tile on entity is
 * standing on and the direction in which the entity is facing (usually the
 * direction the entity as just been moving - the opposite to the direction it
 * came from), it provides methods to encode and decode its field values to and
 * from a single int.
 *
 */
public final class PositionOnTrack implements FreerailsMutableSerializable {

    /**
     *
     */
    public static final int BITS_FOR_COORDINATE = 14;

    /**
     *
     */
    public static final int BITS_FOR_DIRECTION = 3;

    /**
     *
     */
    public static final int MAX_COORDINATE = (1 << BITS_FOR_COORDINATE) - 1;

    /**
     *
     */
    public static final int MAX_DIRECTION = (1 << BITS_FOR_DIRECTION) - 1;

    private static final long serialVersionUID = 3257853198755707184L;
    /**
     * The direction from which we entered the tile.
     */
    private Step cameFrom = Step.NORTH;
    private int x = 0;
    private int y = 0;

    /**
     *
     */
    public PositionOnTrack() {
    }

    /**
     *
     * @param i
     */
    public PositionOnTrack(int i) {
        this.setValuesFromInt(i);
    }

    private PositionOnTrack(int x, int y, Step direction) {
        if (x > MAX_COORDINATE || x < 0) {
            throw new IllegalArgumentException("x=" + x);
        }

        if (y > MAX_COORDINATE || y < 0) {
            throw new IllegalArgumentException("y=" + y);
        }

        this.x = x;
        this.y = y;

        this.cameFrom = direction;
    }

    /**
     *
     * @param x
     * @param y
     * @param direction
     * @return
     */
    public static PositionOnTrack createComingFrom(int x, int y, Step direction) {
        return new PositionOnTrack(x, y, direction);
    }

    /**
     *
     * @param x
     * @param y
     * @param direction
     * @return
     */
    public static PositionOnTrack createFacing(int x, int y, Step direction) {
        return new PositionOnTrack(x, y, direction.getOpposite());
    }

    /**
     *
     * @param ints
     * @return
     */
    public static PositionOnTrack[] fromInts(int[] ints) {
        PositionOnTrack[] returnValue = new PositionOnTrack[ints.length];
        for (int i = 0; i < ints.length; i++) {
            PositionOnTrack p = new PositionOnTrack(ints[i]);
            returnValue[i] = p;
        }
        return returnValue;
    }

    /**
     *
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
     *
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
    public Step cameFrom() {
        return cameFrom;
    }

    @Override
    public boolean equals(Object o) {
        if (null == o) {
            return false;
        }

        if (o instanceof PositionOnTrack) {
            PositionOnTrack other = (PositionOnTrack) o;

            return other.cameFrom() == this.cameFrom()
                    && other.getX() == this.getX()
                    && other.getY() == this.getY();
        }
        return false;
    }

    /**
     * @return The direction the entity is facing.
     */
    public Step facing() {
        return cameFrom.getOpposite();
    }

    /**
     * @return the position on the track which is in the opposite direction.
     */
    public PositionOnTrack getOpposite() {
        int newX = this.getX() - this.cameFrom.deltaX;
        int newY = this.getY() - this.cameFrom.deltaY;
        Step newDirection = this.cameFrom.getOpposite();

        return createComingFrom(newX, newY, newDirection);
    }

    /**
     *
     * @return
     */
    public int getX() {
        return x;
    }

    /**
     *
     * @param x
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     *
     * @return
     */
    public int getY() {
        return y;
    }

    /**
     *
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
     *
     * @param v
     */
    public void setCameFrom(Step v) {
        this.cameFrom = v;
    }

    /**
     *
     * @param v
     */
    public void setFacing(Step v) {
        this.cameFrom = v.getOpposite();
    }

    /**
     *
     * @param i
     */
    public void setValuesFromInt(int i) {
        x = i & MAX_COORDINATE;

        int shiftedY = i & (MAX_COORDINATE << BITS_FOR_COORDINATE);
        y = shiftedY >> BITS_FOR_COORDINATE;

        int shiftedDirection = i & (MAX_DIRECTION << (2 * BITS_FOR_COORDINATE));
        int directionAsInt = shiftedDirection >> (2 * BITS_FOR_COORDINATE);
        cameFrom = Step.getInstance(directionAsInt);
    }

    /**
     *
     * @param step
     */
    public void move(Step step) {
        this.x += step.deltaX;
        this.y += step.deltaY;
        this.cameFrom = step.getOpposite();
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

        return "PositionOnTrack: " + x + ", " + y + " facing "
                + cameFrom.getOpposite().toString();
    }
}