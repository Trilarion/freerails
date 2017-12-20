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
package freerails.world.train;

import java.io.Serializable;

/**
 *
 */
public interface SpeedAgainstTime extends Serializable {

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