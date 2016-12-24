/*
 * Created on 04-Oct-2004
 *
 */
package jfreerails.controller;

import jfreerails.world.accounts.EconomicClimate;
import jfreerails.world.common.Money;
import jfreerails.world.top.ReadOnlyWorld;

/**
 * Not yet implemented
 * 
 * @author Luke
 * 
 */
public class FinancialMoveProducer {
    public static final Money IPO_SHARE_PRICE = new Money(5);

    public static final int SHARE_BUNDLE_SIZE = 10000;

    public static final int IPO_SIZE = SHARE_BUNDLE_SIZE * 10;

    FinancialMoveProducer(ReadOnlyWorld row) {
    }

    EconomicClimate worsen() {
        return null;
    }

    EconomicClimate improve() {
        return null;
    }
}