package freerails.model;

import freerails.model.finances.Money;

import java.nio.charset.Charset;

/**
 *
 */
public final class ModelConstants {

    public static final int STOCK_BUNDLE_SIZE = 10000;
    public static final int LENGTH_OF_STRAIGHT_TRACK_PIECE = 200;
    public static final Money BOND_VALUE_ISSUE = new Money(500000);
    public static final Money BOND_VALUE_REPAY = new Money(-500000);
    /**
     * size of a tile (height and width)
     */
    // TODO the world should not depend on the tile size, currently it still does, maybe a different tile size (not pixel on screen, but kilometer?)
    public static final int TILE_SIZE = 30;
    public static final double TILE_DIAGONAL_SIZE = StrictMath.hypot(TILE_SIZE, TILE_SIZE);
    public static final int WAGON_LENGTH = 24;
    public static final int MAX_NUMBER_OF_WAGONS = 6;
    public static final int MAX_TRAIN_LENGTH = (1 + MAX_NUMBER_OF_WAGONS) * WAGON_LENGTH;
    public static final int TRAIN_CRASH_FRAMES_COUNT = 15;
    public static final int UNITS_OF_CARGO_PER_WAGON = 40;
    public static final Charset defaultCharset = Charset.forName("utf-8"); // used for reading/writing of json
    private static final int SHARE_BUNDLE_SIZE = 10000;
    public static final int IPO_SIZE = SHARE_BUNDLE_SIZE * 10;
    /**
     * The threshold that demand for a cargo must exceed before the station
     * demands the cargo.
     */
    public static final int PREREQUISITE_FOR_DEMAND = 16;
    /**
     * Determines how much the player gets for delivering cargo.
     */
    public static final int CARGO_DELIVERY_EARNINGS_FACTOR = 75;

    private ModelConstants() {
    }
}
