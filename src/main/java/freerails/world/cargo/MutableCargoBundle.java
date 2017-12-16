/*
 * Created on 24-May-2003
 *
 */
package freerails.world.cargo;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * This CargoBundle implementation uses a <code>java.util.SortedMap</code> to
 * map quantities to cargo batches.
 *
 * @author Luke
 */
public class MutableCargoBundle implements CargoBundle {

    private final SortedMap<CargoBatch, Integer> sortedMap;

    private int updateID = 0;

    public MutableCargoBundle() {
        sortedMap = new TreeMap<CargoBatch, Integer>();
    }

    public MutableCargoBundle(ImmutableCargoBundle imcb) {
        this();

        Iterator<CargoBatch> it = imcb.cargoBatchIterator();

        while (it.hasNext()) {
            CargoBatch cb = it.next();
            addCargo(cb, imcb.getAmount(cb));
        }
    }

    public void addCargo(CargoBatch cb, int amount) {
        int amountAlready = this.getAmount(cb);
        this.setAmount(cb, amount + amountAlready);
        updateID++;
    }

    /**
     * Note, calling hasNext() or next() on the returned iterator throws a
     * ConcurrentModificationException if this CargoBundle has changed since the
     * iterator was aquired.
     */
    public Iterator<CargoBatch> cargoBatchIterator() {
        final Iterator<CargoBatch> it = sortedMap.keySet().iterator();

        /*
         * A ConcurrentModificationException used to get thrown when the amount
         * of cargo was set to 0, since this resulted in the key being removed
         * from the hashmap. The iterator below throws a
         * ConcurrentModificationException whenever this CargoBundle has been
         * changed since the iterator was aquired. This should mean that if the
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

    public boolean contains(CargoBatch cb) {
        return sortedMap.containsKey(cb);
    }

    @Override
    public boolean equals(Object arg0) {
        if (null == arg0) {
            return false;
        }

        if (!(arg0 instanceof CargoBundle)) {
            return false;
        }

        return ImmutableCargoBundle.equals(this, (CargoBundle) arg0);
    }

    public int getAmount(CargoBatch cb) {
        if (contains(cb)) {
            Integer i = sortedMap.get(cb);

            return i.intValue();
        }
        return 0;
    }

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

    public void setAmount(CargoBatch cb, int amount) {
        if (0 == amount) {
            sortedMap.remove(cb);
        } else {
            sortedMap.put(cb, new Integer(amount));
        }

        updateID++;
    }

    public int size() {
        return sortedMap.size();
    }

    public ImmutableCargoBundle toImmutableCargoBundle() {
        return new ImmutableCargoBundle(sortedMap);
    }

    @Override
    public String toString() {
        return toImmutableCargoBundle().toString();
    }
}