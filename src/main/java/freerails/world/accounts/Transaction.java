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