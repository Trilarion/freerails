package jfreerails.world.common;


/**
 * This class defines a straight line between two points. Units are arbitrary.
 */
public class IntLine implements FreerailsSerializable {
    public int x1;
    public int x2;
    public int y1;
    public int y2;

    /**
     * @return the length of the line
     */
    public double getLength() {
        int sumOfSquares = (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);

        return Math.sqrt((double)sumOfSquares);
    }

    /**
     * @param xx1 x of the first point
     * @param yy1 y of the first point
     * @param xx2 x of the second point
     * @param yy2 y of the second point
     */
    public IntLine(int xx1, int yy1, int xx2, int yy2) {
        x1 = xx1;
        y1 = yy1;
        x2 = xx2;
        y2 = yy2;
    }

    /**
     * Default constructor - defines a dot at 0,0
     */
    public IntLine() {
    }

    public boolean equals(Object o) {
        if (null == o) {
            return false;
        }

        if (o == this) {
            return true;
        }

        if (o instanceof IntLine) {
            IntLine line = (IntLine)o;

            if (line.x1 == this.x1 && line.x2 == this.x2 && line.y1 == this.y1 &&
                    line.y2 == this.y2) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public String toString() {
        return "(" + x1 + ", " + y1 + ", " + x2 + ", " + y2 + ")";
    }
}