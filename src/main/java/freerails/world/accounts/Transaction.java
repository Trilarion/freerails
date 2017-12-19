package freerails.world.accounts;

import freerails.world.FreerailsSerializable;
import freerails.world.common.Money;

/**
 * A Transaction is a change in a player's bank balance and/or assets.
 *
 */
public interface Transaction extends FreerailsSerializable {

    /**
     *
     * @return
     */
    Money deltaAssets();

    /**
     * Positive means credit.
     *
     * @return 
     */
    Money deltaCash();

    /**
     *
     * @return
     */
    Category getCategory();

    /**
     *
     */
    enum Category {

        /**
         *
         */
        BOND,

        /**
         *
         */
        BRIDGES,

        /**
         *
         */
        CARGO_DELIVERY,

        /**
         *
         */
        INDUSTRIES,

        /**
         *
         */
        INTEREST_CHARGE,

        /**
         *
         */
        ISSUE_STOCK,

        /**
         *
         */
        MISC_INCOME,

        /**
         *
         */
        STATION_MAINTENANCE,

        /**
         *
         */
        STATIONS,

        /**
         *
         */
        TRACK,

        /**
         *
         */
        TRACK_MAINTENANCE,

        /**
         *
         */
        TRAIN,

        /**
         *
         */
        TRAIN_MAINTENANCE,

        /**
         *
         */
        TRANSFER_STOCK
    }
}