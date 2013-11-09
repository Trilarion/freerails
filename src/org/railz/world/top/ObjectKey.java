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

package org.railz.world.top;

import org.railz.world.common.FreerailsSerializable;
import org.railz.world.player.FreerailsPrincipal;

/**
 * Identifies a unique object within a table in the Game World
 */
public class ObjectKey implements FreerailsSerializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4963612205339567261L;
	public final int index;
	public final FreerailsPrincipal principal;
	public final KEY key;

	public ObjectKey(KEY k, FreerailsPrincipal p, int i) {
		key = k;
		principal = p;
		index = i;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof ObjectKey))
			return false;

		ObjectKey ok = (ObjectKey) o;
		return (ok.index == index
				&& ((ok.key == null) ? (key == null) : (ok.key.equals(key))) && ((ok.principal == null) ? (principal == null)
					: ok.principal.equals(principal)));
	}

	public int hashcode() {
		/* TODO should be good enough for most purposes but could be improved */
		return index;
	}
}
