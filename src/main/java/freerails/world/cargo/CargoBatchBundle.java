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
 *
 */
package freerails.world.cargo;

import java.util.Iterator;

// TODO should the amount already be included in the cargo batch

/**
 * Holds a number of cargo batches with an amount for each cargo batch.
 *
 * <table width="75%" border="0">
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
 */

public interface CargoBatchBundle {

    /**
     * Iterator over the cargo batches.
     *
     * Note, calling hasNext() or next() on the returned iterator throws a
     * ConcurrentModificationException if this CargoBatchBundle has changed since the
     * iterator was acquired.
     */
    Iterator<CargoBatch> cargoBatchIterator();

    /**
     * @return True if this cargo batch is contained.
     */
    boolean contains(CargoBatch cargoBatch);

    /**
     * @param cargoBatch A cargo batch.
     * @return The amount of this cargo batch or 0 if the cargo batch is not contained.
     */
    int getAmount(CargoBatch cargoBatch);

    /**
     * Convenience. Iterates over all contained cargo batches and returns the total amount
     * of a specific cargo type.
     *
     * @param cargoType A cargo type.
     * @return The cumulative amount of all contained batches of this type.
     */
    int getAmountOfType(int cargoType);

    /**
     * @return Number of contained cargo batches.
     */
    int size();

}