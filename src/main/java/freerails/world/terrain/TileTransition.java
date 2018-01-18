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
package freerails.world.terrain;

import freerails.util.Point2D;
import freerails.world.WorldConstants;
import freerails.world.track.TrackConfigurations;

/**
 * Represents a movement from a tile to any one of the surrounding
 * eight tiles.
 */
public final class TileTransition implements TrackConfigurations {

    private static final double TILE_DIAGONAL = StrictMath.hypot(WorldConstants.TILE_SIZE, WorldConstants.TILE_SIZE);
    /**
     * North.
     */
    public static final TileTransition NORTH;
    /**
     * West.
     */
    public static final TileTransition WEST;
    /**
     * South East.
     */
    public static final TileTransition SOUTH_EAST;
    /**
     * North-East.
     */
    public static final TileTransition NORTH_EAST;
    /**
     * East.
     */
    public static final TileTransition EAST;
    /**
     * South.
     */
    public static final TileTransition SOUTH;
    /**
     * South West.
     */
    public static final TileTransition SOUTH_WEST;
    /**
     * North West.
     */
    public static final TileTransition NORTH_WEST;
    private static final long serialVersionUID = 3256444698640921912L;
    /**
     * A 3x3 array of OneTileMoveVectors, representing vectors to eight adjacent
     * tiles plus a zero-distance vector.
     */
    private static final TileTransition[][] vectors = setupVectors();

    /**
     * Another array of OneTileMoveVectors representing the 8 compass directions
     * going clockwise from North.
     */
    private static final TileTransition[] list;

    static {
        NORTH = getInstance(0, -1);
        WEST = getInstance(-1, 0);
        SOUTH_EAST = getInstance(1, 1);
        NORTH_EAST = getInstance(1, -1);
        EAST = getInstance(1, 0);
        SOUTH = getInstance(0, 1);
        SOUTH_WEST = getInstance(-1, 1);
        NORTH_WEST = getInstance(-1, -1);

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
        length = (x * y) == 0 ? WorldConstants.TILE_SIZE : TILE_DIAGONAL;
    }

    private static TileTransition[][] setupVectors() {
        int t = 1;
        TileTransition[][] tvectors = new TileTransition[3][3];

        for (int y = -1; y <= 1; y++) {
            for (int x = -1; x <= 1; x++) {
                if ((0 != x) || (0 != y)) {
                    tvectors[x + 1][y + 1] = new TileTransition(x, y, t);
                }

                t = t << 1;
            }
        }

        return tvectors;
    }

    /**
     * @param p
     * @param path
     * @return
     */
    public static Point2D move(Point2D p, TileTransition... path) {
        int x = p.x;
        int y = p.y;
        for (TileTransition v : path) {
            x += v.deltaX;
            y += v.deltaY;
        }
        return new Point2D(x, y);
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
    public static boolean checkValidity(Point2D a, Point2D b) {
        int dx = b.x - a.x;
        int dy = b.y - a.y;
        return checkValidity(dx, dy);
    }

    /**
     * @param dx
     * @param dy
     * @return
     */
    public static TileTransition getInstance(int dx, int dy) {
        if ((((dx < -1) || (dx > 1)) || ((dy < -1) || (dy > 1))) || ((dx == 0) && (dy == 0))) {
            throw new IllegalArgumentException(dx + " and " + dy + ": The values passed both must be integers in the range -1 to 1, and not both equal 0.");
        }
        return vectors[dx + 1][dy + 1];
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
    public static TileTransition[] getList() {
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

    /**
     * Returns the X component of the vector.
     */
    public int getDx() {
        return deltaX;
    }

    /**
     * Returns the Y component of the vector.
     */
    public int getDy() {
        return deltaY;
    }

    /**
     * Returns a new oneTileMoveVector whose direction is opposite to that the
     * current one.
     *
     * @return A oneTileMoveVector.
     */
    public TileTransition getOpposite() {
        return getInstance(deltaX * -1, deltaY * -1);
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
    public Point2D createRelocatedPoint(Point2D from) {
        return new Point2D(from.x + deltaX, from.y + deltaY);
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
        return TileTransition.getInstance(deltaX, deltaY);
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