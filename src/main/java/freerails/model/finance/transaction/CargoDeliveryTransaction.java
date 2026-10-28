/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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

import java.io.Serial;
import java.util.Objects;

// TODO Is this an Item transaction or a money transaction or something more complex?
/**
 * A credit for delivering cargo.
 */
public class CargoDeliveryTransaction extends Transaction {

    @Serial
    private static final long serialVersionUID = 3257009851963160372L;
    private final CargoBatch cargoBatch;
    private final int quantity;
    private final int stationId;
    private final int trainId;

    /**
     * Records payment for a cargo delivery, including its batch and route
     * endpoints.
     *
     * @param amount payment credited for the delivery
     * @param time game time when delivery is recorded
     * @param quantity number of cargo units delivered
     * @param stationId destination station identifier
     * @param trainId delivering train identifier
     * @param cargoBatch batch from which the delivered cargo came
     */
    public CargoDeliveryTransaction(Money amount, Time time, int quantity, int stationId, int trainId, CargoBatch cargoBatch) {
        super(TransactionCategory.CARGO_DELIVERY, amount, time);
        this.stationId = stationId;
        this.quantity = quantity;
        this.cargoBatch = cargoBatch;
        this.trainId = trainId;
    }

    /**
     * Returns the identifier of the train that made the delivery.
     *
     * @return delivering train identifier
     */
    public int getTrainId() {
        return trainId;
    }

    /**
     * Returns the cargo batch from which the delivery was taken.
     *
     * @return delivered cargo batch
     */
    public CargoBatch getCargoBatch() {
        return cargoBatch;
    }

    /**
     * Returns the number of cargo units delivered.
     *
     * @return delivered quantity
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Returns the identifier of the station that received the delivery.
     *
     * @return receiving station identifier
     */
    public int getStationId() {
        return stationId;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        CargoDeliveryTransaction other = (CargoDeliveryTransaction) obj;
        return quantity == other.quantity
                && stationId == other.stationId
                && trainId == other.trainId
                && Objects.equals(cargoBatch, other.cargoBatch);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), cargoBatch, quantity, stationId, trainId);
    }
}