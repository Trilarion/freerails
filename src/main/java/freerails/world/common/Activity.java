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
