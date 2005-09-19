/*
 * Created on 21-Jun-2003
 *
 */
package jfreerails.world.accounts;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.Money;

/**
 * A Transaction is a change in a player's bank balance and/or assets.
 * 
 * @author Luke Lindsay
 * 
 */
public interface Transaction extends FreerailsSerializable {

	public enum Category {
		BOND, BRIDGES, CARGO_DELIVERY, INDUSTRIES, INTEREST_CHARGE, ISSUE_STOCK, MISC_INCOME, STATION_MAINTENANCE, STATIONS, TRACK, TRACK_MAINTENANCE, TRAIN, TRAIN_MAINTENANCE, TRANSFER_STOCK
	}

    Money deltaAssets();
    
    /** Positive means credit. */
    Money deltaCash();

	Category getCategory();
}