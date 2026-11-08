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

import freerails.util.IntPoint2D;
import freerails.model.ModelConstants;
import freerails.model.track.TrackConfigurations;

import java.io.Serial;

/**
 * Represents one of the eight unit transitions between adjacent map tiles.
 */
public class TileTransition implements TrackConfigurations {

    /**
     * Represents the eight compass directions with their canonical displacement and
     * track-template bit.
     */
    public enum TileDirection {
        NORTH(0, -1, 1),
        NORTH_EAST(1, -1, 2),
        EAST(1, 0, 4),
        SOUTH_EAST(1, 1, 8),
        SOUTH(0, 1, 16),
        SOUTH_WEST(-1, 1, 32),
        WEST(-1, 0, 64),
        NORTH_WEST(-1, -1, 128);

        private static final TileDirection[] COMPASS_ORDER = {
                NORTH, NORTH_EAST, EAST, SOUTH_EAST, SOUTH, SOUTH_WEST, WEST, NORTH_WEST
        };

        public final int deltaX;
        public final int deltaY;
        private final int configurationBit;

        TileDirection(int deltaX, int deltaY, int configurationBit) {
            this.deltaX = deltaX;
            this.deltaY = deltaY;
            this.configurationBit = configurationBit;
        }

        public static TileDirection fromDelta(int dx, int dy) {
            for (TileDirection direction : values()) {
                if (direction.deltaX == dx && direction.deltaY == dy) {
                    return direction;
                }
            }
            throw new IllegalArgumentException("No tile direction for delta (" + dx + ", " + dy + ")");
        }

        public static TileDirection fromCompassIndex(int index) {
            return COMPASS_ORDER[index];
        }

        public TileDirection getOpposite() {
            return switch (this) {
                case NORTH -> SOUTH;
                case NORTH_EAST -> SOUTH_WEST;
                case EAST -> WEST;
                case SOUTH_EAST -> NORTH_WEST;
                case SOUTH -> NORTH;
                case SOUTH_WEST -> NORTH_EAST;
                case WEST -> EAST;
                case NORTH_WEST -> SOUTH_EAST;
            };
        }

        public int getDeltaX() {
            return deltaX;
        }

        public int getDeltaY() {
            return deltaY;
        }

        public int getConfigurationBit() {
            return configurationBit;
        }
    }

    @Serial
    private static final long serialVersionUID = 3256444698640921912L;

    public static final TileTransition NORTH = new TileTransition(TileDirection.NORTH);
    public static final TileTransition NORTH_WEST = new TileTransition(TileDirection.NORTH_WEST);
    public static final TileTransition WEST = new TileTransition(TileDirection.WEST);
    public static final TileTransition SOUTH_WEST = new TileTransition(TileDirection.SOUTH_WEST);
    public static final TileTransition SOUTH = new TileTransition(TileDirection.SOUTH);
    public static final TileTransition SOUTH_EAST = new TileTransition(TileDirection.SOUTH_EAST);
    public static final TileTransition EAST = new TileTransition(TileDirection.EAST);
    public static final TileTransition NORTH_EAST = new TileTransition(TileDirection.NORTH_EAST);

    /**
     * A 3x3 array of OneTileMoveVectors, representing vectors to eight adjacent
     * tiles plus a zero-distance vector.
     */
    private static final TileTransition[][] vectors;

    static {
        vectors = new TileTransition[3][3];

        for (TileDirection direction : TileDirection.values()) {
            vectors[direction.deltaX + 1][direction.deltaY + 1] = switch (direction) {
                case NORTH -> NORTH;
                case NORTH_EAST -> NORTH_EAST;
                case EAST -> EAST;
                case SOUTH_EAST -> SOUTH_EAST;
                case SOUTH -> SOUTH;
                case SOUTH_WEST -> SOUTH_WEST;
                case WEST -> WEST;
                case NORTH_WEST -> NORTH_WEST;
            };
        }
    }

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
    private final int configurationBit;
    private final double length;
    private final TileDirection direction;

    private TileTransition(TileDirection direction) {
        this.direction = direction;
        deltaX = direction.deltaX;
        deltaY = direction.deltaY;
        configurationBit = direction.configurationBit;
        length = deltaX * deltaY == 0 ? ModelConstants.MODEL_UNITS_PER_TILE : ModelConstants.MODEL_DIAGONAL_TILE_LENGTH;
    }

    /**
     * @param p starting map coordinate
     * @param path transitions to apply in order
     * @return the coordinate reached after applying all transitions
     */
    public static IntPoint2D move(IntPoint2D p, TileTransition... path) {
        int x = p.x();
        int y = p.y();
        for (TileTransition v : path) {
            x += v.deltaX;
            y += v.deltaY;
        }
        return new IntPoint2D(x, y);
    }

    /**
     * @param number compass direction index, from 0 (north) through 7
     * @return the canonical transition at that index
     */
    public static TileTransition getInstance(int number) {
        return list[number];
    }

    public TileDirection getTileDirection() {
        return direction;
    }

    /**
     * Tests whether two coordinates are distinct neighbors.
     *
     * @param a starting coordinate
     * @param b destination coordinate
     * @return {@code true} if the coordinates differ by at most one in each
     *         axis and are not equal
     */
    public static boolean checkValidity(IntPoint2D a, IntPoint2D b) {
        int dx = b.x() - a.x();
        int dy = b.y() - a.y();
        return dx >= -1 && dx <= 1 && dy >= -1 && dy <= 1 && (dx != 0 || dy != 0);
    }

    /**
     * @return the canonical transition represented by the displacement
     * @throws IllegalArgumentException if the displacement is zero or exceeds one tile
     */
    public static TileTransition getInstance(IntPoint2D d) {
        if (d.x() < -1 || d.x() > 1 || d.y() < -1 || d.y() > 1 || d.x() == 0 && d.y() == 0) {
            throw new IllegalArgumentException(d.x() + " and " + d.y() + ": The values passed both must be integers in the range -1 to 1, and not both equal 0.");
        }
        return vectors[d.x() + 1][d.y() + 1];
    }

    public static TileTransition getInstance(TileDirection direction) {
        return switch (direction) {
            case NORTH -> NORTH;
            case NORTH_EAST -> NORTH_EAST;
            case EAST -> EAST;
            case SOUTH_EAST -> SOUTH_EAST;
            case SOUTH -> SOUTH;
            case SOUTH_WEST -> SOUTH_WEST;
            case WEST -> WEST;
            case NORTH_WEST -> NORTH_WEST;
        };
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
    public IntPoint2D getD() { return new IntPoint2D(deltaX, deltaY); }

    /**
     * Returns the canonical transition pointing in the opposite direction.
     *
     * @return the reverse transition
     */
    public TileTransition getOpposite() {
        return getInstance(new IntPoint2D(-deltaX , -deltaY ));
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
    public IntPoint2D createRelocatedPoint(IntPoint2D from) {
        return new IntPoint2D(from.x() + deltaX, from.y() + deltaY);
    }

    /**
     * Returns this transition's length in fixed-point model distance units.
     *
     * @return model units for orthogonal transitions and diagonal transitions
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
        return TileTransition.getInstance(new IntPoint2D(deltaX, deltaY));
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
    @Override
    public int get8bitTemplate() {
        return configurationBit;
    }

    /**
     * Checks whether a track configuration consists of exactly this direction.
     *
     * @param configuration track configuration to inspect
     * @return {@code true} if its directional mask matches this transition
     */
    public boolean contains(TrackConfigurations configuration) {
        return configuration.get8bitTemplate() == configurationBit;
    }
}