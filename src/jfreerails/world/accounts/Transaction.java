/*
 * Created on 21-Jun-2003
 *
 */
package jfreerails.world.accounts;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.Money;


/**
 * @author Luke Lindsay
 *
 */
public interface Transaction extends FreerailsSerializable {
    /* Transaction categories.*/
    public static final int TRACK = 0;
    public static final int CARGO_DELIVERY = 1;
    public static final int TRACK_MAINTENANCE = 2;
    public static final int TRAIN_MAINTENANCE = 3;
    public static final int STATION_MAINTENANCE = 4;
    public static final int TRAIN = 5;
    public static final int MISC_INCOME = 6;
    public static final int INTEREST_CHARGE = 7;
    public static final int BOND = 8;
    public static final int EQUITY = 9;

    /** Positive means credit. */
    Money getValue();

    int getCategory();
}