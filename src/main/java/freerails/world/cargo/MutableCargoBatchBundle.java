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

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * This CargoBatchBundle implementation uses a {@code java.util.SortedMap} to
 * map quantities to cargo batches.
 */
public class MutableCargoBatchBundle implements CargoBatchBundle {

    private final SortedMap<CargoBatch, Integer> cargoMap;
    private int updateID = 0;

    public MutableCargoBatchBundle() {
        cargoMap = new TreeMap<>();
    }

    /**
     * @param immutableCargoBatchBundle
     */
    public MutableCargoBatchBundle(ImmutableCargoBatchBundle immutableCargoBatchBundle) {
        this();

        Iterator<CargoBatch> it = immutableCargoBatchBundle.cargoBatchIterator();

        while (it.hasNext()) {
            CargoBatch cargoBatch = it.next();
            addCargo(cargoBatch, immutableCargoBatchBundle.getAmount(cargoBatch));
        }
    }

    /**
     * Adds a cargo batch with a certain amount.
     *
     * @param cargoBatch
     * @param amount
     */
    public void addCargo(CargoBatch cargoBatch, int amount) {
        setAmount(cargoBatch, amount + getAmount(cargoBatch));
        updateID++;
    }

    /**
     * Note, calling hasNext() or next() on the returned iterator throws a
     * ConcurrentModificationException if this CargoBatchBundle has changed since the
     * iterator was acquired.
     *
     * @return
     */
    public Iterator<CargoBatch> cargoBatchIterator() {
        final Iterator<CargoBatch> it = cargoMap.keySet().iterator();

        // TODO Does Java already has a nonmo
        /*
         * A ConcurrentModificationException used to get thrown when the amount
         * of cargo was set to 0, since this resulted in the key being removed
         * from the hash map. The iterator below throws a
         * ConcurrentModificationException whenever this CargoBatchBundle has been
         * changed since the iterator was acquired. This should mean that if the
         * cargo bundle gets changed while the iterator is in use, you will know
         * about it straight away.
         */
        return new Iterator<CargoBatch>() {
            final int updateIDAtCreation = updateID;

            public boolean hasNext() {
                if (updateIDAtCreation != updateID) {
                    throw new ConcurrentModificationException();
                }

                return it.hasNext();
            }

            public CargoBatch next() {
                if (updateIDAtCreation != updateID) {
                    throw new ConcurrentModificationException();
                }

                return it.next();
            }

            public void remove() {
                throw new UnsupportedOperationException(
                        "Use CargoBatchBundle.setAmount(CargoBatch cb, 0)");
            }
        };
    }

    /**
     * @param cargoBatch
     * @return
     */
    public boolean contains(CargoBatch cargoBatch) {
        return cargoMap.containsKey(cargoBatch);
    }

    @Override
    public boolean equals(Object arg0) {
        if (null == arg0) {
            return false;
        }
        return arg0 instanceof CargoBatchBundle && ImmutableCargoBatchBundle.equals(this, (CargoBatchBundle) arg0);
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

        updateID++;
    }

    public int size() {
        return cargoMap.size();
    }

    /**
     * @return
     */
    public ImmutableCargoBatchBundle toImmutableCargoBundle() {
        return new ImmutableCargoBatchBundle(this);
    }

    // TODO this involves some crazy copying just for the name, make the interface abstract instead
    @Override
    public String toString() {
        return toImmutableCargoBundle().toString();
    }
}