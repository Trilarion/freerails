package freerails.world;

// TODO not many constants here

import freerails.world.finances.Money;

/**
 *
 */
public final class WorldConstants {

    public static final int STOCK_BUNDLE_SIZE = 10000;
    public static final int LENGTH_OF_STRAIGHT_TRACK_PIECE = 200;
    public static final Money BOND_VALUE_ISSUE = new Money(500000);
    public static final Money BOND_VALUE_REPAY = new Money(-500000);
    /**
     * size of a tile (height and width)
     */
    public static final int TILE_SIZE = 30;
    public static final double TILE_DIAGONAL_SIZE = StrictMath.hypot(TILE_SIZE, TILE_SIZE);
    private static final int SHARE_BUNDLE_SIZE = 10000;
    public static final int IPO_SIZE = SHARE_BUNDLE_SIZE * 10;
    /**
     * The threshold that demand for a cargo must exceed before the station
     * demands the cargo.
     */
    public static final int PREREQUISITE_FOR_DEMAND = 16;

    private WorldConstants() {
    }
}
