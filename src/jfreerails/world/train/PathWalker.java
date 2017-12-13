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

package jfreerails.world.train;

import jfreerails.world.common.FreerailsPathIterator;
import jfreerails.world.common.FreerailsSerializable;


/** This interface lets the caller retrieve a path broken into
 * a series of steps, whose length the caller specifies.
 * E.g. it could be used to get the sub section of a path that
 * a train travels during an given time inteval.
 */
public interface PathWalker extends FreerailsPathIterator, FreerailsSerializable {
    /** Returns true if we have not reached the end of the path.
     */
    boolean canStepForward();

    /** Moves this path walker forward to the end to the path.
     * and returns a path iterator to retrieve the path
     * travelled during this move.
     */

    //void stepForward();

    /** Moves this path walker forward by the specified
     * distance along the path and returns a path iterator
     * to retrieve the section of the path travelled
     * during this move.
     */
    void stepForward(double distance);
}