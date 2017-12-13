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

package jfreerails.world.player;

import java.security.Principal;
import jfreerails.world.common.FreerailsSerializable;


/**
 * This interface identifies a principal. This interface may be extended in
 * the future in order to provide faster lookups, rather than using name
 * comparisons.
 *
 * A principal represents an entity which can view or alter the game world. A
 * principal usually corresponds to a player's identity, but may also
 * represent an authorititative server, or a another game entity such as a
 * corporation. All entities which may own game world objects must be
 * represented by a principal.
 */
public abstract class FreerailsPrincipal implements Principal,
    FreerailsSerializable {
}