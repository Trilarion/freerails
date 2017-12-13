/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.railz.move;

import org.railz.world.top.KEY;
import org.railz.world.common.FreerailsSerializable;


/**
 * This interface provides information about changes to the lists in the World database.
 */
public interface ListMove extends Move {
    /**
     * @return the type of object which was changed
     */
    public KEY getKey();

    /**
     * @return the old item or null if not any.
     */
    public FreerailsSerializable getBefore();

    /**
     * @return the new item or null if not any.
     */
    public FreerailsSerializable getAfter();

    /**
     * @return the index of the item which changed.
     */
    public int getIndex();
}
