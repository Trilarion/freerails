/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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

import freerails.util.Vec2D;
import freerails.model.ModelConstants;
import freerails.model.track.TrackConfigurations;

import java.io.Serial;

/**
 * Represents one of the eight unit transitions between adjacent map tiles.
 */
public class TileTransition implements TrackConfigurations {

    // TODO TileDirection could become an enum and TileTransition vectors some composite

    @Serial
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
                if (0 != x || 0 != y) {
                    vectors[x + 1][y + 1] = new TileTransition(x, y, t);
                }
                t = t << 1;
            }
        }
    }

    public static final TileTransition NORTH = getInstance(new Vec2D(0, -1));
    public static final TileTransition NORTH_WEST = getInstance(new Vec2D(-1, -1));
    public static final TileTransition WEST = getInstance(new Vec2D(-1, 0));
    public static final TileTransition SOUTH_WEST = getInstance(new Vec2D(-1, 1));
    public static final TileTransition SOUTH = getInstance(new Vec2D(0, 1));
    public static final TileTransition SOUTH_EAST = getInstance(new Vec2D(1, 1));
    public static final TileTransition EAST = getInstance(new Vec2D(1, 0));
    public static final TileTransition NORTH_EAST = getInstance(new Vec2D(1, -1));

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
     * Creates a transition. The private constructor ensures instances are
     * canonical; use {@link #getInstance(Vec2D)} to retrieve one.
     *
     * @param x horizontal displacement, either -1, 0, or 1
     * @param y vertical displacement, either -1, 0, or 1
     * @param t bit template identifying the corresponding track direction
     */
    private TileTransition(int x, int y, int t) {
        deltaX = x;
        deltaY = y;
        flatTrackTemplate = t;
        length = x * y == 0 ? ModelConstants.TILE_SIZE : ModelConstants.TILE_DIAGONAL_SIZE;
    }

    /**
     * @param p starting map coordinate
     * @param path transitions to apply in order
     * @return the coordinate reached after applying all transitions
     */
    public static Vec2D move(Vec2D p, TileTransition... path) {
        int x = p.x();
        int y = p.y();
        for (TileTransition v : path) {
            x += v.deltaX;
            y += v.deltaY;
        }
        return new Vec2D(x, y);
    }

    /**
     * @param number compass direction index, from 0 (north) through 7
     * @return the canonical transition at that index
     */
    public static TileTransition getInstance(int number) {
        return list[number];
    }

    /**
     * Tests whether two coordinates are distinct neighbors.
     *
     * @param a starting coordinate
     * @param b destination coordinate
     * @return {@code true} if the coordinates differ by at most one in each
     *         axis and are not equal
     */
    public static boolean checkValidity(Vec2D a, Vec2D b) {
        int dx = b.x() - a.x();
        int dy = b.y() - a.y();
        return dx >= -1 && dx <= 1 && dy >= -1 && dy <= 1 && (dx != 0 || dy != 0);
    }

    /**
     * @return the canonical transition represented by the displacement
     * @throws IllegalArgumentException if the displacement is zero or exceeds one tile
     */
    public static TileTransition getInstance(Vec2D d) {
        if (d.x() < -1 || d.x() > 1 || d.y() < -1 || d.y() > 1 || d.x() == 0 && d.y() == 0) {
            throw new IllegalArgumentException(d.x() + " and " + d.y() + ": The values passed both must be integers in the range -1 to 1, and not both equal 0.");
        }
        return vectors[d.x() + 1][d.y() + 1];
    }

    /**
     * @return a defensive copy of the eight transitions, clockwise from north
     */
    public static TileTransition[] getTransitions() {
        return list.clone(); // defensive copy.
    }

    /**
     * Selects the compass transition closest in direction to a displacement.
     *
     * @param dx horizontal displacement
     * @param dy vertical displacement
     * @return the nearest of the eight compass transitions
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
     * Returns this transition's displacement as a vector.
     *
     * @return a new vector containing the horizontal and vertical displacement
     */
    public Vec2D getD() { return new Vec2D(deltaX, deltaY); }

    /**
     * Returns the canonical transition pointing in the opposite direction.
     *
     * @return the reverse transition
     */
    public TileTransition getOpposite() {
        return getInstance(new Vec2D(-deltaX , -deltaY ));
    }

    /**
     * Returns the direction name, such as {@code "north east"}.
     *
     * @return this transition's compass direction name
     */
    @Override
    public String toString() {
        String name = switch (deltaY) {
            case 1 -> " south";
            case -1 -> " north";
            default -> "";
        };

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
     * @return this transition's compact compass abbreviation, such as {@code "ne"}
     */
    public String toAbrvString() {
        String name = switch (deltaY) {
            case 1 -> "s";
            case -1 -> "n";
            default -> "";
        };

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
     * @param from coordinate to move
     * @return the translated coordinate
     */
    public Vec2D createRelocatedPoint(Vec2D from) {
        return new Vec2D(from.x() + deltaX, from.y() + deltaY);
    }

    /**
     * Checks whether a track configuration consists of exactly this direction.
     *
     * @param ftt track configuration to inspect
     * @return {@code true} if its nine-bit template matches this transition
     */
    public boolean contains(TrackConfigurations ftt) {
        return ftt.get9bitTemplate() == flatTrackTemplate;
    }

    /** {@inheritDoc} */
    @Override
    public int get9bitTemplate() {
        return flatTrackTemplate;
    }

    /**
     * Returns the geometric length of this transition using the model's tile
     * dimensions.
     *
     * @return tile-size distance for orthogonal transitions, diagonal distance
     *         for diagonal transitions
     */
    public double getLength() {
        return length;
    }

    /**
     * @return angle in radians, increasing clockwise from north
     */
    public double getDirection() {
        int i = 0;

        while (this != list[i]) {
            i++;
        }

        return 2 * Math.PI / 8 * i;
    }

    /**
     * @return 0 for north, 1 for north-east, through 7 for north-west
     */
    public int getID() {
        int i = 0;

        while (this != list[i]) {
            i++;
        }

        return i;
    }

    /**
     * Resolves deserialized instances to their canonical singleton.
     *
     * @return the canonical instance for this displacement
     */
    @Serial
    private Object readResolve() {
        return TileTransition.getInstance(new Vec2D(deltaX, deltaY));
    }

    /**
     * @return {@code true} if this transition changes both coordinates
     */
    public boolean isDiagonal() {
        return 0 != deltaX * deltaY;
    }

    /**
     * @return a one-hot eight-bit template for this compass direction
     */
    public int get8bitTemplate() {
        return 1 << getID();
    }
}