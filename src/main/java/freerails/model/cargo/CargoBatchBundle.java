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

import java.util.*;

/**
 * This CargoBatchBundle implementation uses a {@code java.util.SortedMap} to map quantities to cargo batches.
 * Represents a bundle of cargo made up of quantities of cargo from
 * different {@link CargoBatch}s.
 */
public class CargoBatchBundle implements UnmodifiableCargoBatchBundle {

    public static final UnmodifiableCargoBatchBundle EMPTY = new CargoBatchBundle();
    private final SortedMap<CargoBatch, Integer> cargoMap;

    /** Creates an empty mutable cargo bundle. */
    public CargoBatchBundle() {
        cargoMap = new TreeMap<>();
    }

    /**
     * Creates a mutable copy of the contents of another bundle.
     *
     * @param cargoBatchBundle bundle whose contents are copied
     */
    public CargoBatchBundle(UnmodifiableCargoBatchBundle cargoBatchBundle) {
        this();

        for (CargoBatch cargoBatch: cargoBatchBundle.getCargoBatches()) {
            addCargo(cargoBatch, cargoBatchBundle.getAmount(cargoBatch));
        }
    }

    /**
     * Adds the supplied amount to the existing quantity of the batch.
     *
     * @param cargoBatch batch whose quantity is increased
     * @param amount quantity to add
     */
    public void addCargo(CargoBatch cargoBatch, int amount) {
        setAmount(cargoBatch, amount + getAmount(cargoBatch));
    }

    /**
     * Returns an unmodifiable snapshot of the batches currently in the bundle.
     *
     * @return unmodifiable collection of present batches
     */
    @Override
    public Collection<CargoBatch> getCargoBatches() {
        return Collections.unmodifiableCollection(new ArrayList<>(cargoMap.keySet()));
    }

    /**
     * Tests whether the bundle contains an entry for the batch.
     *
     * @param cargoBatch batch to look up
     * @return {@code true} if the batch has a nonzero stored quantity
     */
    @Override
    public boolean contains(CargoBatch cargoBatch) {
        return cargoMap.containsKey(cargoBatch);
    }

    /**
     * Tests whether another mutable bundle has the same batch quantities.
     *
     * @param other object to compare
     * @return {@code true} if both bundles contain equal mappings
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (null == other) {
            return false;
        }
        if (!(other instanceof CargoBatchBundle o)) {
            return false;
        }
        return cargoMap.equals(o.cargoMap);
    }



    /**
     * Sums the quantities of all batches with the specified cargo type.
     *
     * @param cargoType cargo type identifier
     * @return total quantity for that type
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

    /**
     * Returns a hash code based on the number of distinct stored batches.
     *
     * @return hash code for this bundle
     */
    @Override
    public int hashCode() {
        return cargoMap.size();
    }

    /**
     * Returns the quantity of a batch, or zero when it is absent.
     *
     * @param cargoBatch batch to look up
     * @return stored quantity, or zero if absent
     */
    @Override
    public int getAmount(CargoBatch cargoBatch) {
        if (contains(cargoBatch)) {
            return cargoMap.get(cargoBatch);
        }
        return 0;
    }

    /**
     * Sets a batch's quantity, removing its entry when the quantity is zero.
     *
     * @param cargoBatch batch to update
     * @param amount new quantity
     */
    public void setAmount(CargoBatch cargoBatch, int amount) {
        if (0 == amount) {
            cargoMap.remove(cargoBatch);
        } else {
            cargoMap.put(cargoBatch, amount);
        }
    }

    /**
     * Returns the number of batches with nonzero quantities.
     *
     * @return number of stored batches
     */
    @Override
    public int size() {
        return cargoMap.size();
    }

    /**
     * Returns a multi-line representation of this bundle's batch quantities.
     *
     * @return formatted bundle contents
     */
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