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

package freerails.model.finance.transaction;

import freerails.model.cargo.CargoBatch;
import freerails.model.finance.Money;
import freerails.model.game.Time;

// TODO Is this an Item transaction or a money transaction or something more complex?
/**
 * A credit for delivering cargo.
 */
public class CargoDeliveryTransaction extends Transaction {

    private static final long serialVersionUID = 3257009851963160372L;
    private final CargoBatch cargoBatch;
    private final int quantity;
    private final int stationId;
    private final int trainId;

    /**
     * @param amount
     * @param quantity
     * @param stationId
     * @param trainId
     * @param cargoBatch
     */
    public CargoDeliveryTransaction(Money amount, Time time, int quantity, int stationId, int trainId, CargoBatch cargoBatch) {
        super(TransactionCategory.CARGO_DELIVERY, amount, time);
        this.stationId = stationId;
        this.quantity = quantity;
        this.cargoBatch = cargoBatch;
        this.trainId = trainId;
    }

    /**
     * @return
     */
    public int getTrainId() {
        return trainId;
    }

    /**
     * @return
     */
    public CargoBatch getCargoBatch() {
        return cargoBatch;
    }

    /**
     * @return
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * @return
     */
    public int getStationId() {
        return stationId;
    }
}