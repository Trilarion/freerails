/*
 * Created on 02-Jul-2005
 *
 */
package freerails.world.common;

/**
 *
 * @author jkeller1
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
