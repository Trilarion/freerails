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

    private WorldConstants() {
    }
}
