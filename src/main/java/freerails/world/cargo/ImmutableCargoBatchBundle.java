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

import java.io.Serializable;
import java.util.*;

/**
 * Represents a bundle of cargo made up of quantities of cargo from
 * different {@link CargoBatch}s.

 */
public class ImmutableCargoBatchBundle implements CargoBatchBundle, Serializable {

    private static final long serialVersionUID = 3257566187666814009L;
    public static final ImmutableCargoBatchBundle EMPTY_CARGO_BATCH_BUNDLE = new ImmutableCargoBatchBundle();
    private final SortedMap<CargoBatch, Integer> cargoMap;

    private ImmutableCargoBatchBundle() {
        cargoMap = Collections.emptySortedMap();
    }

    /**
     * Copies and wraps in an unmodifieable map, making it immutable.
     * 
     * @param cargoBatchBundle
     */
    public ImmutableCargoBatchBundle(CargoBatchBundle cargoBatchBundle) {
        SortedMap<CargoBatch, Integer> map = new TreeMap<>();

        Iterator<CargoBatch> it = cargoBatchBundle.cargoBatchIterator();

        while (it.hasNext()) {
            CargoBatch cargoBatch = it.next();
            map.put(cargoBatch, cargoBatchBundle.getAmount(cargoBatch));
        }
        
        cargoMap = Collections.unmodifiableSortedMap(map);
    }

    /**
     * @param a
     * @param b
     * @return
     */
    public static boolean equals(CargoBatchBundle a, CargoBatchBundle b) {
        if (a.size() != b.size())
            return false;

        Iterator<CargoBatch> it = a.cargoBatchIterator();
        while (it.hasNext()) {
            CargoBatch batch = it.next();
            if (a.getAmount(batch) != b.getAmount(batch)) {
                return false;
            }
        }
        return true;

    }

    public Iterator<CargoBatch> cargoBatchIterator() {
        return cargoMap.keySet().iterator();
    }

    /**
     * @param cargoBatch
     * @return
     */
    public boolean contains(CargoBatch cargoBatch) {
        return cargoMap.containsKey(cargoBatch);
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }
        return obj instanceof CargoBatchBundle && equals(this, (CargoBatchBundle) obj);
    }

    /**
     * @param cargoBatch
     * @return
     */
    public int getAmount(CargoBatch cargoBatch) {
        if (contains(cargoBatch)) {
            return cargoMap.get(cargoBatch);
        }
        return 0;
    }

    /**
     * @param cargoType
     * @return
     */
    public int getAmountOfType(int cargoType) {
        int amount = 0;
        for (CargoBatch cargoBatch : cargoMap.keySet()) {
            if (cargoBatch.getCargoType() == cargoType) {
                amount += getAmount(cargoBatch);
            }
        }
        return amount;
    }

    @Override
    public int hashCode() {
        return cargoMap.size();
    }

    /**
     * @return
     */
    public int size() {
        return cargoMap.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CargoBatchBundle {\n");

        for (CargoBatch cargoBatch : cargoMap.keySet()) {
            sb.append(cargoMap.get(cargoBatch));
            sb.append(" units of cargo type ");
            sb.append(cargoBatch);
            sb.append('\n');
        }
        sb.append('}');

        return sb.toString();
    }
}