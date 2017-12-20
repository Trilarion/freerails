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
 * This CargoBundle implementation uses a {@code java.util.SortedMap} to
 * map quantities to cargo batches.
 */
public class MutableCargoBundle implements CargoBundle {

    private final SortedMap<CargoBatch, Integer> sortedMap;

    private int updateID = 0;

    /**
     *
     */
    public MutableCargoBundle() {
        sortedMap = new TreeMap<>();
    }

    /**
     * @param imcb
     */
    public MutableCargoBundle(ImmutableCargoBundle imcb) {
        this();

        Iterator<CargoBatch> it = imcb.cargoBatchIterator();

        while (it.hasNext()) {
            CargoBatch cb = it.next();
            addCargo(cb, imcb.getAmount(cb));
        }
    }

    /**
     * @param cb
     * @param amount
     */
    public void addCargo(CargoBatch cb, int amount) {
        int amountAlready = this.getAmount(cb);
        this.setAmount(cb, amount + amountAlready);
        updateID++;
    }

    /**
     * Note, calling hasNext() or next() on the returned iterator throws a
     * ConcurrentModificationException if this CargoBundle has changed since the
     * iterator was acquired.
     *
     * @return
     */
    public Iterator<CargoBatch> cargoBatchIterator() {
        final Iterator<CargoBatch> it = sortedMap.keySet().iterator();

        /*
         * A ConcurrentModificationException used to get thrown when the amount
         * of cargo was set to 0, since this resulted in the key being removed
         * from the hash map. The iterator below throws a
         * ConcurrentModificationException whenever this CargoBundle has been
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
                        "Use CargoBundle.setAmount(CargoBatch cb, 0)");
            }
        };
    }

    /**
     * @param cb
     * @return
     */
    public boolean contains(CargoBatch cb) {
        return sortedMap.containsKey(cb);
    }

    @Override
    public boolean equals(Object arg0) {
        if (null == arg0) {
            return false;
        }

        return arg0 instanceof CargoBundle && ImmutableCargoBundle.equals(this, (CargoBundle) arg0);

    }

    /**
     * @param cb
     * @return
     */
    public int getAmount(CargoBatch cb) {
        if (contains(cb)) {

            return sortedMap.get(cb);
        }
        return 0;
    }

    /**
     * @param cargoType
     * @return
     */
    public int getAmount(int cargoType) {
        Iterator<CargoBatch> it = cargoBatchIterator();
        int amount = 0;

        while (it.hasNext()) {
            CargoBatch cb = it.next();

            if (cb.getCargoType() == cargoType) {
                amount += getAmount(cb);
            }
        }

        return amount;
    }

    @Override
    public int hashCode() {
        return sortedMap.size();
    }

    /**
     * @param cb
     * @param amount
     */
    public void setAmount(CargoBatch cb, int amount) {
        if (0 == amount) {
            sortedMap.remove(cb);
        } else {
            sortedMap.put(cb, amount);
        }

        updateID++;
    }

    /**
     * @return
     */
    public int size() {
        return sortedMap.size();
    }

    /**
     * @return
     */
    public ImmutableCargoBundle toImmutableCargoBundle() {
        return new ImmutableCargoBundle(sortedMap);
    }

    @Override
    public String toString() {
        return toImmutableCargoBundle().toString();
    }
}