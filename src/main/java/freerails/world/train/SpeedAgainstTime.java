/*
 * Created on 10-Jul-2005
 *
 */
package freerails.world.train;

import freerails.world.common.FreerailsSerializable;

/**
 *
 * @author jkeller1
 */
public interface SpeedAgainstTime extends FreerailsSerializable {

    /**
     * Returns the distance travelled at time t. The returned value, s,
     * satisfies the following conditions:
     * <ol>
     * <li>s >= 0</li>
     * <li>s <= getS()</li>
     * <li>s = 0 if t = 0 </li>
     * <li>s = getS() if t = getT()</li>
     * </ol>
     *
     * @param t
     * @return s
     * @throws IllegalArgumentException iff t < 0 or t > getT()
     */
    double calcS(double t);

    /**
     * Returns the time taken to travel distance s. The returned value, t,
     * satisfies the following conditions:
     * <ol>
     * <li>t >= 0</li>
     * <li>t <= getT()</li>
     * <li>t = 0 if s = 0 </li>
     * <li>t = getT() if s = getS()</li>
     * </ol>
     *
     * @param s
     * @return t
     * @throws IllegalArgumentException iff s < 0 or s > getS()
     */
    double calcT(double s);

    /**
     * @param t
     * @return 
     * @throws IllegalArgumentException iff t < 0 or t > getT()
     */
    double calcV(double t);

    /**
     * @param t
     * @return 
     * @throws IllegalArgumentException iff t < 0 or t > getT()
     */
    double calcA(double t);

    /**
     * @return The time taken to travel the distance given by getS().
     */
    double getT();

    /**
     * @return The distance traveled during at time given by getT().
     */
    double getS();

}