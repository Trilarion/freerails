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

/*
 * Created on 04-Oct-2004
 *
 */
package freerails.controller;

import freerails.world.finances.EconomicClimate;
import freerails.world.finances.Money;
import freerails.world.top.ReadOnlyWorld;

/**
 * Not yet implemented
 */
public class FinancialMoveProducer {

    /**
     *
     */
    public static final Money IPO_SHARE_PRICE = new Money(5);

    /**
     *
     */
    public static final int SHARE_BUNDLE_SIZE = 10000;

    /**
     *
     */
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