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

/*
 *
 */
package freerails.model.cargo;

import java.io.Serializable;
import java.util.Collection;

// TODO should the amount already be included in the cargo batch?
/**
 * Holds a number of cargo batches with an amount for each cargo batch.
 *
 * <table>
 * <caption>Example</caption>
 * <tr>
 * <td><strong>Cargo Batch</strong></td>
 * <td><strong>Amount</strong></td>
 * </tr>
 * <tr>
 * <td>passengers from (1, 5) created at 01:00</td>
 * <td>2</td>
 * </tr>
 * <tr>
 * <td>passengers from (1, 5) created at 01:25</td>
 * <td>1</td>
 * </tr>
 * <tr>
 * <td>coal from (4,10) created at 02:50</td>
 * <td>8</td>
 * </tr>
 * <tr>
 * <td>mail from (6, 10) created at 04:45</td>
 * <td>10</td>
 * </tr>
 * </table>
 *
 * Cargo bundles are used to represent the cargo carried by trains and the cargo waiting at stations).
 */
public interface UnmodifiableCargoBatchBundle extends Serializable {

    /**
     * Returns a collection snapshot of the batches present at call time.
     *
     * @return batches currently in the bundle
     */
    Collection<CargoBatch> getCargoBatches();

    /**
     * Tests whether the specified batch is present.
     *
     * @param cargoBatch batch to test
     * @return {@code true} if the bundle contains the batch
     */
    boolean contains(CargoBatch cargoBatch);

    /**
     * Returns the amount associated with a batch.
     *
     * @param cargoBatch batch to look up
     * @return its amount, or zero if absent
     */
    int getAmount(CargoBatch cargoBatch);

    /**
     * Sums the amounts of all batches belonging to the specified cargo type.
     *
     * @param cargoType A cargo type.
     * @return The cumulative amount of all contained batches of this type.
     */
    int getAmountOfType(int cargoType);

    /**
     * Returns the number of batches present in the bundle.
     *
     * @return number of contained batches
     */
    int size();

}