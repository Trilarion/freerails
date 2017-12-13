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

package org.railz.client.model;

import java.awt.Point;

public interface MapCursor {

    /**
     *  A MapCursor that does nothing.
     */
    public static final MapCursor NULL_MAP_CURSOR = new MapCursor() {

	public void tryMoveCursor(Point tryThisPoint) {
	}

	public void addCursorEventListener(CursorEventListener l) {
	}

	public void removeCursorEventListener(CursorEventListener l) {
	}

	public void setMessage(String message) {
	}
    };

    /**
     *  Moves the cursor provided the destination is a legal position.
     * @param tryThisPoint The cursor's destination.
     */
    public void tryMoveCursor(Point tryThisPoint);

    /**
     *  Adds a listener.  Listeners could include: the trackbuild system, the
     * view the cursor moves across, etc.
     * @param l The listener.
     */
    public void addCursorEventListener(CursorEventListener l);

    public void removeCursorEventListener(CursorEventListener l);

    public void setMessage(String message);
}
