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

package freerails.world.cargo;

import freerails.util.ImInts;
import freerails.util.ImList;

import java.io.Serializable;
import java.util.Iterator;
import java.util.SortedMap;

/**
 * This class represents a bundle of cargo made up of quantities of cargo from
 * different {@link CargoBatch}s.
 * <table width="75%" border="0">
 * <caption>Example</caption>
 * <tr>
 * <td><strong>Cargo Batch</strong></td>
 * <td><strong>Quantity</strong></td>
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
public class ImmutableCargoBundle implements CargoBundle, Serializable {

    /**
     *
     */
    public static final ImmutableCargoBundle EMPTY_BUNDLE = new ImmutableCargoBundle();

    private static final long serialVersionUID = 3257566187666814009L;
    private final ImInts amounts;
    private final ImList<CargoBatch> batches;

    private ImmutableCargoBundle() {
        batches = new ImList<>();
        amounts = new ImInts();
    }

    /**
     * @param sortedMap
     */
    public ImmutableCargoBundle(SortedMap<CargoBatch, Integer> sortedMap) {
        int size = sortedMap.size();
        int[] amountsArray = new int[size];
        CargoBatch[] batchesArray = new CargoBatch[size];
        int i = 0;
        for (CargoBatch batch : sortedMap.keySet()) {
            batchesArray[i] = batch;
            amountsArray[i] = sortedMap.get(batch);
            i++;
        }

        batches = new ImList<>(batchesArray);
        amounts = new ImInts(amountsArray);
    }

    /**
     * @param a
     * @param b
     * @return
     */
    public static boolean equals(CargoBundle a, CargoBundle b) {
        Iterator<CargoBatch> it = a.cargoBatchIterator();
        if (a.size() != b.size())
            return false;
        while (it.hasNext()) {
            CargoBatch batch = it.next();

            if (a.getAmount(batch) != b.getAmount(batch)) {
                return false;
            }
        }
        return true;

    }

    public Iterator<CargoBatch> cargoBatchIterator() {
        return new Iterator<CargoBatch>() {
            int index = 0;

            public boolean hasNext() {
                return index < batches.size();
            }

            public CargoBatch next() {
                CargoBatch o = batches.get(index);
                index++;

                return o;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * @param cargoBatch
     * @return
     */
    public boolean contains(CargoBatch cargoBatch) {
        for (int i = 0; i < batches.size(); i++) {
            if (batches.get(i).equals(cargoBatch)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean equals(Object arg0) {
        if (null == arg0) {
            return false;
        }

        return arg0 instanceof CargoBundle && equals(this, (CargoBundle) arg0);

    }

    /**
     * @param cargoBatch
     * @return
     */
    public int getAmount(CargoBatch cargoBatch) {
        int amount = 0;

        for (int i = 0; i < batches.size(); i++) {
            if (batches.get(i).equals(cargoBatch)) {
                amount += amounts.get(i);
            }
        }

        return amount;
    }

    // 666 use dynamic cache (growing arraylist)->breaks save games

    /**
     * @param cargoType
     * @return
     */
    public int getAmount(int cargoType) {
        int amount = 0;
        for (int i = 0; i < batches.size(); i++) {
            if (batches.get(i).getCargoType() == cargoType) {
                amount += amounts.get(i);
            }
        }

        return amount;
    }

    @Override
    public int hashCode() {
        return amounts.size();
    }

    /**
     * @return
     */
    public int size() {
        return batches.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CargoBundle {\n");

        for (int i = 0; i < batches.size(); i++) {
            sb.append(amounts.get(i));
            sb.append(" units of cargo type ");
            sb.append(batches.get(i));
            sb.append("\n");
        }

        sb.append("}");

        return sb.toString();
    }
}