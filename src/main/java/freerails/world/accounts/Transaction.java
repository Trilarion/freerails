/*
 * Created on 21-Jun-2003
 *
 */
package freerails.world.accounts;

import freerails.world.common.FreerailsSerializable;
import freerails.world.common.Money;

/**
 * A Transaction is a change in a player's bank balance and/or assets.
 *
 * @author Luke Lindsay
 */
public interface Transaction extends FreerailsSerializable {

    Money deltaAssets();

    /**
     * Positive means credit.
     */
    Money deltaCash();

    Category getCategory();

    enum Category {
        BOND, BRIDGES, CARGO_DELIVERY, INDUSTRIES, INTEREST_CHARGE, ISSUE_STOCK, MISC_INCOME, STATION_MAINTENANCE, STATIONS, TRACK, TRACK_MAINTENANCE, TRAIN, TRAIN_MAINTENANCE, TRANSFER_STOCK
    }
}