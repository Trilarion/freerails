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
package freerails.model.cargo;

import java.util.*;

/**
 * This CargoBatchBundle implementation uses a {@code java.util.SortedMap} to map quantities to cargo batches.
 * Represents a bundle of cargo made up of quantities of cargo from
 * different {@link CargoBatch}s.
 */
public class CargoBatchBundle implements UnmodifiableCargoBatchBundle {

    public static final UnmodifiableCargoBatchBundle EMPTY = new CargoBatchBundle();
    private final SortedMap<CargoBatch, Integer> cargoMap;

    public CargoBatchBundle() {
        cargoMap = new TreeMap<>();
    }

    /**
     * @param cargoBatchBundle
     */
    public CargoBatchBundle(UnmodifiableCargoBatchBundle cargoBatchBundle) {
        this();

        for (CargoBatch cargoBatch: cargoBatchBundle.getCargoBatches()) {
            addCargo(cargoBatch, cargoBatchBundle.getAmount(cargoBatch));
        }
    }

    /**
     * Adds a cargo batch with a certain amount.
     */
    public void addCargo(CargoBatch cargoBatch, int amount) {
        setAmount(cargoBatch, amount + getAmount(cargoBatch));
    }

    /**
     * Makes a copy.
     *
     * @return
     */
    @Override
    public Collection<CargoBatch> getCargoBatches() {
        return Collections.unmodifiableCollection(new ArrayList<>(cargoMap.keySet()));
    }

    /**
     * @param cargoBatch
     * @return
     */
    @Override
    public boolean contains(CargoBatch cargoBatch) {
        return cargoMap.containsKey(cargoBatch);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (null == other) {
            return false;
        }
        if (!(other instanceof CargoBatchBundle)) {
            return false;
        }
        CargoBatchBundle o = (CargoBatchBundle) other;
        return cargoMap.equals(o.cargoMap);
    }



    /**
     * @param cargoType
     * @return
     */
    @Override
    public int getAmountOfType(int cargoType) {
        int amount = 0;
        for (CargoBatch cargoBatch : cargoMap.keySet()) {
            if (cargoBatch.getCargoTypeId() == cargoType) {
                amount += getAmount(cargoBatch);
            }
        }
        return amount;
    }

    @Override
    public int hashCode() {
        return cargoMap.size();
    }

    @Override
    public int getAmount(CargoBatch cargoBatch) {
        if (contains(cargoBatch)) {
            return cargoMap.get(cargoBatch);
        }
        return 0;
    }

    /**
     * @param cargoBatch
     * @param amount
     */
    public void setAmount(CargoBatch cargoBatch, int amount) {
        if (0 == amount) {
            cargoMap.remove(cargoBatch);
        } else {
            cargoMap.put(cargoBatch, amount);
        }
    }

    @Override
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