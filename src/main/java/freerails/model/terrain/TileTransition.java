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

/*
 * OneTileMoveVector.java
 *
 */
package freerails.model.terrain;

import freerails.util.Vector2D;
import freerails.model.WorldConstants;
import freerails.model.track.TrackConfigurations;

/**
 * Represents a movement from a tile to any one of the surrounding
 * eight tiles.
 */
public class TileTransition implements TrackConfigurations {

    // TODO TileDirection could become an enum and TileTransition vectors some composite

    private static final long serialVersionUID = 3256444698640921912L;

    /**
     * A 3x3 array of OneTileMoveVectors, representing vectors to eight adjacent
     * tiles plus a zero-distance vector.
     */
    private static final TileTransition[][] vectors;

    static {
        vectors = new TileTransition[3][3];

        int t = 1;
        for (int y = -1; y <= 1; y++) {
            for (int x = -1; x <= 1; x++) {
                if ((0 != x) || (0 != y)) {
                    vectors[x + 1][y + 1] = new TileTransition(x, y, t);
                }
                t = t << 1;
            }
        }
    }

    public static final TileTransition NORTH = getInstance(new Vector2D(0, -1));
    public static final TileTransition NORTH_WEST = getInstance(new Vector2D(-1, -1));
    public static final TileTransition WEST = getInstance(new Vector2D(-1, 0));
    public static final TileTransition SOUTH_WEST = getInstance(new Vector2D(-1, 1));
    public static final TileTransition SOUTH = getInstance(new Vector2D(0, 1));
    public static final TileTransition SOUTH_EAST = getInstance(new Vector2D(1, 1));
    public static final TileTransition EAST = getInstance(new Vector2D(1, 0));
    public static final TileTransition NORTH_EAST = getInstance(new Vector2D(1, -1));

    /**
     * Another array of TileTransitions representing the 8 compass directions going clockwise from North.
     */
    private static final TileTransition[] list;

    static {
        list = new TileTransition[8];
        list[0] = NORTH;
        list[1] = NORTH_EAST;
        list[2] = EAST;
        list[3] = SOUTH_EAST;
        list[4] = SOUTH;
        list[5] = SOUTH_WEST;
        list[6] = WEST;
        list[7] = NORTH_WEST;
    }

    /**
     * The X and Y components of the vector.
     */
    public final int deltaX;
    /**
     * The X and Y components of the vector.
     */
    public final int deltaY;
    private final int flatTrackTemplate;
    private final double length;

    /**
     * Create a new OneTileMoveVector. N.B Private constructor to enforce enum
     * property, use getInstance(x,y) instead. Pass values for delta X and Y:
     * they must be in the range -1 to 1 and cannot both be equal to 0.
     *
     * @param x CityTile coordinate.
     * @param y CityTile coordinate
     * @param t an integer representing the track template this vector
     *          corresponds to.
     */
    private TileTransition(int x, int y, int t) {
        deltaX = x;
        deltaY = y;
        flatTrackTemplate = t;
        length = (x * y) == 0 ? WorldConstants.TILE_SIZE : WorldConstants.TILE_DIAGONAL_SIZE;
    }

    /**
     * @param p
     * @param path
     * @return
     */
    public static Vector2D move(Vector2D p, TileTransition... path) {
        int x = p.x;
        int y = p.y;
        for (TileTransition v : path) {
            x += v.deltaX;
            y += v.deltaY;
        }
        return new Vector2D(x, y);
    }

    /**
     * @param number
     * @return
     */
    public static TileTransition getInstance(int number) {
        return list[number];
    }

    /**
     * @param a
     * @param b
     * @return
     */
    public static boolean checkValidity(Vector2D a, Vector2D b) {
        int dx = b.x - a.x;
        int dy = b.y - a.y;
        return checkValidity(dx, dy);
    }

    /**
     * @return
     */
    public static TileTransition getInstance(Vector2D d) {
        if ((((d.x < -1) || (d.x > 1)) || ((d.y < -1) || (d.y > 1))) || ((d.x == 0) && (d.y == 0))) {
            throw new IllegalArgumentException(d.x + " and " + d.y + ": The values passed both must be integers in the range -1 to 1, and not both equal 0.");
        }
        return vectors[d.x + 1][d.y + 1];
    }

    /**
     * Returns true if the values passed could be used to create a valid vector.
     */
    private static boolean checkValidity(int x, int y) {
        return (((x >= -1) && (x <= 1)) && ((y >= -1) && (y <= 1))) && ((x != 0) || (y != 0));
    }

    /**
     * @return a copy of the list of 8 OneTileMoveVectors going clockwise from
     * North.
     */
    public static TileTransition[] getTransitions() {
        return list.clone(); // defensive copy.
    }

    /**
     * @return the OneTileMoveVector nearest in orientation to the specified dx,
     * dy
     */
    public static TileTransition getNearestVector(int dx, int dy) {
        if (0 == dx * dy) {
            if (dx > 0) {
                return EAST;
            } else if (dx != 0) {
                return WEST;
            } else if (dy > 0) {
                return SOUTH;
            } else {
                return NORTH;
            }
        }

        double gradient = dy;
        gradient = gradient / dx;

        double B = 2;
        double A = 0.5;
        double C = -2;
        double D = -0.5;

        if (gradient > B) {
            if (dy < 0) {
                return NORTH;
            }
            return SOUTH;
        } else if (gradient > A) {
            if (dy > 0) {
                return SOUTH_EAST;
            }
            return NORTH_WEST;
        } else if (gradient > D) {
            if (dx > 0) {
                return EAST;
            }
            return WEST;
        } else if (gradient > C) {
            if (dx < 0) {
                return SOUTH_WEST;
            }
            return NORTH_EAST;
        } else {
            if (dy > 0) {
                return SOUTH;
            }
            return NORTH;
        }
    }

    public Vector2D getD() { return new Vector2D(deltaX, deltaY); }

    /**
     * Returns a new oneTileMoveVector whose direction is opposite to that the
     * current one.
     *
     * @return A oneTileMoveVector.
     */
    public TileTransition getOpposite() {
        return getInstance(new Vector2D(-deltaX , -deltaY ));
    }

    /**
     * Returns the name of the vector. E.g. "north-east"
     *
     * @return the name.
     */
    @Override
    public String toString() {
        String name;

        switch (deltaY) {
            case 1:
                name = " south";

                break;

            case -1:
                name = " north";

                break;

            default:
                name = "";

                break;
        }

        switch (deltaX) {
            case 1:
                name += " east";

                break;

            case -1:
                name += " west";

                break;

            default:
                break;
        }

        return name;
    }

    /**
     * @return
     */
    public String toAbrvString() {
        String name;

        switch (deltaY) {
            case 1:
                name = "s";

                break;

            case -1:
                name = "n";

                break;

            default:
                name = "";

                break;
        }

        switch (deltaX) {
            case 1:
                name += "e";

                break;

            case -1:
                name += "w";

                break;

            default:
                break;
        }

        return name;
    }

    /**
     * @param from
     * @return
     */
    public Vector2D createRelocatedPoint(Vector2D from) {
        return new Vector2D(from.x + deltaX, from.y + deltaY);
    }

    public boolean contains(TrackConfigurations ftt) {
        return ftt.get9bitTemplate() == flatTrackTemplate;
    }

    public int get9bitTemplate() {
        return flatTrackTemplate;
    }

    /**
     * @return the length of this vector. Each tile is 100 units x 100 units.
     */
    public double getLength() {
        return length;
    }

    /**
     * @return
     */
    public double getDirection() {
        int i = 0;

        while (this != list[i]) {
            i++;
        }

        return 2 * Math.PI / 8 * i;
    }

    /**
     * @return a number representing the compass point this vector indicates,
     * with 0 representing North, 1 NorthEast, 2 East and so on.
     */
    public int getID() {
        int i = 0;

        while (this != list[i]) {
            i++;
        }

        return i;
    }

    private Object readResolve() {
        return TileTransition.getInstance(new Vector2D(deltaX, deltaY));
    }

    /**
     * @return
     */
    public boolean isDiagonal() {
        return 0 != deltaX * deltaY;
    }

    /**
     * @return
     */
    public int get8bitTemplate() {
        return 1 << getID();
    }
}