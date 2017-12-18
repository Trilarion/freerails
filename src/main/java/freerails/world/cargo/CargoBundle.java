/*
 * Created on 12-Aug-2005
 *
 */
package freerails.world.cargo;

import java.util.Iterator;

/**
 *
 * @author jkeller1
 */
public interface CargoBundle {

    /**
     * Note, calling hasNext() or next() on the returned iterator throws a
     * ConcurrentModificationException if this CargoBundle has changed since the
     * iterator was aquired.
     * @return 
     */
    Iterator<CargoBatch> cargoBatchIterator();

    /**
     *
     * @param cb
     * @return
     */
    boolean contains(CargoBatch cb);

    /**
     *
     * @param cb
     * @return
     */
    int getAmount(CargoBatch cb);

    /**
     *
     * @param cargoType
     * @return
     */
    int getAmount(int cargoType);

    /**
     *
     * @return
     */
    int size();

}