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
    Money getValue();
}