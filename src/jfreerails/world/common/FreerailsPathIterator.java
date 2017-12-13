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

package jfreerails.world.common;


/** This interface lets the caller retrieve a path made
 * up of a series of straight lines.  E.g. it lets the
 * path a train takes across a section of track be
 * retrieved without revealing the underlying objects
 * that represent the track.
 */
public interface FreerailsPathIterator extends FreerailsSerializable {
    /** Tests whether the path has another segment.
     */
    boolean hasNext();

    /** Gets the next segment of the path and places its
         * coordinates in the specified IntLine; then moves the
         * iterator forwards by one path segment.  (The coordinates are
         * placed the passed-in IntLine rather than a new object to
         * avoid the cost of object creation.)
         * @param line
         */
    void nextSegment(IntLine line);
}