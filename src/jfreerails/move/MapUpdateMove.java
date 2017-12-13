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

package jfreerails.move;

import java.awt.Rectangle;


/**This interface tags Moves that change items on the map and
 * tells the caller which tiles have been updated.  It is used by
 * the map-view classes to determine which tiles need repainting.
 *
 *
 */
public interface MapUpdateMove extends Move {
    Rectangle getUpdatedTiles();
}