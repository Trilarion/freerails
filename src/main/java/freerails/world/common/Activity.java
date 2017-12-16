/*
 * Created on 02-Jul-2005
 *
 */
package freerails.world.common;

public interface Activity<E extends FreerailsSerializable> extends
        FreerailsSerializable {

    double duration();

    E getState(double dt);

}
