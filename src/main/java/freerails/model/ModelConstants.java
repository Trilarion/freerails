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

package freerails.model;

import freerails.model.economy.Money;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Shared model dimensions, economic values, and gameplay limits.
 */
public final class ModelConstants {

    public static final int STOCK_BUNDLE_SIZE = 10000;
    public static final int LENGTH_OF_STRAIGHT_TRACK_PIECE = 200;
    public static final Money BOND_VALUE_ISSUE = new Money(500000);
    public static final Money BOND_VALUE_REPAY = new Money(-500000);
    /** Fixed-point model distance units per map tile. */
    public static final int MODEL_UNITS_PER_TILE = 100;
    public static final double MODEL_DIAGONAL_TILE_LENGTH =
            StrictMath.hypot(MODEL_UNITS_PER_TILE, MODEL_UNITS_PER_TILE);
    /** Preserves existing track pricing when distances are expressed in the new model units. */
    public static final double TRACK_COST_PER_MODEL_UNIT = 0.3d;
    public static final int WAGON_LENGTH = 80;
    public static final int MAX_NUMBER_OF_WAGONS = 6;
    public static final int MAX_TRAIN_LENGTH = (1 + MAX_NUMBER_OF_WAGONS) * WAGON_LENGTH;
    public static final int TRAIN_CRASH_FRAMES_COUNT = 15;
    public static final int UNITS_OF_CARGO_PER_WAGON = 40;
    public static final Charset defaultCharset = StandardCharsets.UTF_8; // used for reading/writing of json
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
    public static final int MAX_MAP_DIMENSION = 10_000;
    public static final long MAX_MAP_TILE_COUNT = 1_000_000L;

    /** Prevents instantiation of this constants class. */
    private ModelConstants() {
    }
}
