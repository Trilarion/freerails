/*
 * Created on 12-Aug-2005
 *
 */
package freerails.world.cargo;

import java.util.Iterator;

public interface CargoBundle {

    /**
     * Note, calling hasNext() or next() on the returned iterator throws a
     * ConcurrentModificationException if this CargoBundle has changed since the
     * iterator was aquired.
     */
    Iterator<CargoBatch> cargoBatchIterator();

    boolean contains(CargoBatch cb);

    int getAmount(CargoBatch cb);

    int getAmount(int cargoType);

    int size();

}