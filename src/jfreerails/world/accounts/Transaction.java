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
    
	public enum Category{TRACK, CARGO_DELIVERY, TRACK_MAINTENANCE, TRAIN_MAINTENANCE, STATION_MAINTENANCE, TRAIN, MISC_INCOME, INTEREST_CHARGE, BOND, ISSUE_STOCK, INDUSTRIES, STATIONS, BRIDGES};	  

    /** Positive means credit. */
    Money getValue();

    Category getCategory();
}