package jfreerails.world.common;


/**
 * Represents a position on the track as a direction in one of 8 compass points
 * and a coordinate representing the displacement from the centre of a track
 * tile.
 */
public final class PositionOnTrack implements FreerailsSerializable {
    public static final int BITS_FOR_COORINATE = 14;
    public static final int BITS_FOR_DIRECTION = 3;
    public static final int MAX_COORINATE = (1 << BITS_FOR_COORINATE) - 1;
    public static final int MAX_DIRECTION = (1 << BITS_FOR_DIRECTION) - 1;
    private int x = 0;
    private int y = 0;
    private OneTileMoveVector direction = OneTileMoveVector.NORTH;

    public PositionOnTrack(int x, int y, OneTileMoveVector direction) {
        if (x > MAX_COORINATE || x < 0) {
            throw new IllegalArgumentException("x=" + x);
        }

        if (y > MAX_COORINATE || y < 0) {
            throw new IllegalArgumentException("y=" + y);
        }

        this.x = x;
        this.y = y;

        this.direction = direction;
    }

    public PositionOnTrack(int i) {
        this.setValuesFromInt(i);
    }

    public PositionOnTrack() {
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public OneTileMoveVector getDirection() {
        return direction;
    }

    /**
     * @return an integer representing this PositionOnTrack object
     */
    public int toInt() {
        int i = 0;
        i = i | x;

        int shiftedY = y << BITS_FOR_COORINATE;
        i = i | shiftedY;

        int directionAsInt = direction.getNumber();
        int shiftedDirection = (directionAsInt << (2 * BITS_FOR_COORINATE));
        i = i | shiftedDirection;

        return i;
    }

    public void setValuesFromInt(int i) {
        x = i & MAX_COORINATE;

        int shiftedY = i & (MAX_COORINATE << BITS_FOR_COORINATE);
        y = shiftedY >> BITS_FOR_COORINATE;

        int shiftedDirection = i & (MAX_DIRECTION << (2 * BITS_FOR_COORINATE));
        int directionAsInt = shiftedDirection >> (2 * BITS_FOR_COORINATE);
        direction = OneTileMoveVector.getInstance(directionAsInt);
    }

    public boolean equals(Object o) {
        if (null == o) {
            return false;
        }

        if (o instanceof PositionOnTrack) {
            PositionOnTrack other = (PositionOnTrack)o;

            if (other.getDirection() == this.getDirection() &&
                    other.getX() == this.getX() && other.getY() == this.getY()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public String toString() {
        String s = "PositionOnTrack: " + x + ", " + y + ", " +
            direction.toString();

        return s;
    }

    /**
     * Sets the direction.
     * @param direction The direction to set
     */
    public void setDirection(OneTileMoveVector direction) {
        this.direction = direction;
    }

    /**
     * Sets the x.
     * @param x The x to set
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Sets the y.
     * @param y The y to set
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * @return the position on the track which is in the opposite direction
     * and displacement.
     */
    public PositionOnTrack getOpposite() {
        int newX = this.getX() - this.direction.deltaX;
        int newY = this.getY() - this.direction.deltaY;
        OneTileMoveVector newDirection = this.direction.getOpposite();

        return new PositionOnTrack(newX, newY, newDirection);
    }
}