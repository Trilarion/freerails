package jfreerails.world.common;


/**
 * A <b>mutable</b> class that stores the coordinates of the tile on entity is standing on
 * and the direction in which the entity is facing (usually the direction the entity
 * as just been moving - the opposite to the direction it came from), it provides methods to encode and decode its field
 * values to and from a single int.
 *
 * @author Luke
 */
public final class PositionOnTrack implements FreerailsMutableSerializable {
    private static final int BITS_FOR_COORINATE = 14;
    private static final int BITS_FOR_DIRECTION = 3;
    public static final int MAX_COORINATE = (1 << BITS_FOR_COORINATE) - 1;
    public static final int MAX_DIRECTION = (1 << BITS_FOR_DIRECTION) - 1;
    /** The direction from which we entered the tile.*/
    private OneTileMoveVector direction = OneTileMoveVector.NORTH;
    private int x = 0;

    private int y = 0;
    
    public static int[] toInts(PositionOnTrack[] pos){
    	int[] returnValue = new int[pos.length];
    	for(int i = 0; i < pos.length; i++){
    		returnValue[i] = pos[i].toInt();
    	}
    	return returnValue;
    }
    
    public static PositionOnTrack[] fromInts(int[] ints){
    	PositionOnTrack[] returnValue = new PositionOnTrack[ints.length];
    	for(int i = 0; i < ints.length; i++){
    		PositionOnTrack p = new PositionOnTrack(ints[i]);
    		returnValue[i] = p;
    	}
    	return returnValue;
    }

    public static PositionOnTrack createComingFrom(int x, int y, OneTileMoveVector direction) {
		return new PositionOnTrack(x, y, direction);
	}
    
    public static PositionOnTrack createFacing(int x, int y, OneTileMoveVector direction) {
		return new PositionOnTrack(x, y, direction.getOpposite());
	}

	public PositionOnTrack() {
    }

    public PositionOnTrack(int i) {
        this.setValuesFromInt(i);
    }

    private PositionOnTrack(int x, int y, OneTileMoveVector direction) {
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

    public boolean equals(Object o) {
        if (null == o) {
            return false;
        }

        if (o instanceof PositionOnTrack) {
            PositionOnTrack other = (PositionOnTrack)o;

            if (other.cameFrom() == this.cameFrom() &&
                    other.getX() == this.getX() && other.getY() == this.getY()) {
                return true;
            }
			return false;
        }
		return false;
    }

    /**
     * @return The direction the entity came from.
     */
    public OneTileMoveVector cameFrom() {
        return direction;
    }
    
    /**
     * @return The direction the entity is facing. 
     */
    public OneTileMoveVector facing() {
        return direction.getOpposite();
    }

    /**
     * @return the position on the track which is in the opposite direction
     * and displacement.
     */
    public PositionOnTrack getOpposite() {
        int newX = this.getX() - this.direction.deltaX;
        int newY = this.getY() - this.direction.deltaY;
        OneTileMoveVector newDirection = this.direction.getOpposite();

        return createComingFrom(newX, newY, newDirection);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int hashCode() {
        int result;
        result = x;
        result = 29 * result + y;
        result = 29 * result + direction.hashCode();

        return result;
    }

    /**
     * Sets the direction.
     * @param direction The direction to set
     */
    public void setDirection(OneTileMoveVector direction) {
        this.direction = direction;
    }

    public void setValuesFromInt(int i) {
        x = i & MAX_COORINATE;

        int shiftedY = i & (MAX_COORINATE << BITS_FOR_COORINATE);
        y = shiftedY >> BITS_FOR_COORINATE;

        int shiftedDirection = i & (MAX_DIRECTION << (2 * BITS_FOR_COORINATE));
        int directionAsInt = shiftedDirection >> (2 * BITS_FOR_COORINATE);
        direction = OneTileMoveVector.getInstance(directionAsInt);
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
     * @return an integer representing this PositionOnTrack object
     */
    public int toInt() {
        int i = x;

        int shiftedY = y << BITS_FOR_COORINATE;
        i = i | shiftedY;

        int directionAsInt = direction.getID();
        int shiftedDirection = (directionAsInt << (2 * BITS_FOR_COORINATE));
        i = i | shiftedDirection;

        return i;
    }

    public String toString() {
        String s = "PositionOnTrack: " + x + ", " + y + ", " +
            direction.toString();

        return s;
    }
}