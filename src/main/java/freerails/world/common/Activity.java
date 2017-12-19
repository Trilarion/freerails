/*
 * Created on 02-Jul-2005
 *
 */
package freerails.world.common;

import freerails.world.FreerailsSerializable;

/**
 *
 * @param <E>
 */
public interface Activity<E extends FreerailsSerializable> extends
        FreerailsSerializable {

    /**
     *
     * @return
     */
    double duration();

    /**
     *
     * @param dt
     * @return
     */
    E getState(double dt);

}
