/*
 * Created on 24-May-2003
 *
 */
package jfreerails.world.cargo;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;


/**This CargoBundle implementation uses a <code>java.util.HashMap</code> to
 * map quantities to cargo batches.
 *
 * @author Luke
 *
 */
public class CargoBundleImpl implements CargoBundle {
    private final HashMap hashMap;
    private int updateID = 0;

    public String toString() {
        String s = "CargoBundle {\n";
        Iterator it = this.cargoBatchIterator();

        while (it.hasNext()) {
            CargoBatch cb = (CargoBatch)it.next();
            s += this.getAmount(cb) + " units of cargo type " +
            cb.getCargoType() + "\n";
        }

        s += "}";

        return s;
    }

    public CargoBundleImpl() {
        hashMap = new HashMap();
    }

    private CargoBundleImpl(HashMap hm) {
        hashMap = hm;
    }

    protected HashMap getHashMap() {
        return hashMap;
    }

    public int getAmount(int cargoType) {
        Iterator it = cargoBatchIterator();
        int amount = 0;

        while (it.hasNext()) {
            CargoBatch cb = (CargoBatch)it.next();

            if (cb.getCargoType() == cargoType) {
                amount += getAmount(cb);
            }
        }

        return amount;
    }

    public int getAmount(CargoBatch cb) {
        if (contains(cb)) {
            Integer i = (Integer)hashMap.get(cb);

            return i.intValue();
        } else {
            return 0;
        }
    }

    public void setAmount(CargoBatch cb, int amount) {
        if (0 == amount) {
            hashMap.remove(cb);
        } else {
            hashMap.put(cb, new Integer(amount));
        }

        updateID++;
    }

    public boolean contains(CargoBatch cb) {
        return hashMap.containsKey(cb);
    }

    /** Note, calling  hasNext() or next() on the returned iterator throws a ConcurrentModificationException
     * if this CargoBundle has changed since the iterator was aquired.
     */
    public Iterator cargoBatchIterator() {
        final Iterator it = hashMap.keySet().iterator();

        /* A ConcurrentModificationException used to get thrown when the amount
             * of cargo was set to 0, since this resulted in the key being removed
             * from the hashmap.  The iterator below throws a ConcurrentModificationException
             * whenever this CargoBundle has been changed since the iterator was aquired.  This should
             * mean that if the cargo bundle gets changed while the iterator is in use, you will know
             * about it straight away.
             */
        return new Iterator() {
                int updateIDAtCreation = updateID;

                public void remove() {
                    throw new UnsupportedOperationException(
                        "Use CargoBundle.setAmount(CargoBatch cb, 0)");
                }

                public boolean hasNext() {
                    if (updateIDAtCreation != updateID) {
                        throw new ConcurrentModificationException();
                    }

                    return it.hasNext();
                }

                public Object next() {
                    if (updateIDAtCreation != updateID) {
                        throw new ConcurrentModificationException();
                    }

                    return it.next();
                }
            };
    }

    public boolean equals(Object o) {
        if (o instanceof CargoBundleImpl) {
            CargoBundleImpl test = (CargoBundleImpl)o;

            return hashMap.equals(test.getHashMap());
        } else {
            return false;
        }
    }

    public CargoBundle getCopy() {
        return new CargoBundleImpl((HashMap)this.hashMap.clone());
    }

    public void addCargo(CargoBatch cb, int amount) {
        int amountAlready = this.getAmount(cb);
        this.setAmount(cb, amount + amountAlready);
        updateID++;
    }
}