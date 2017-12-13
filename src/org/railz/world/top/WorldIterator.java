/*
 * Copyright (C) 2003 Luke Lindsay
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

/*
 * Created on 29-Mar-2003
 *
 */
package org.railz.world.top;

import org.railz.world.common.FreerailsSerializable;


/** This interface lets the caller access the results of
 * a search in the gameworld.  It is similar in concept to
 * <code>java.sql.ResultSet</code>.
 *
 * @author Luke
 *
 */
public interface WorldIterator {
    public static final int BEFORE_FIRST = -1;

    /**Moves the cursor down one row from its current position.
     */
    boolean next();

    /**Moves the cursor up one row from its current position.
    */
    boolean previous();

    /** Moves the cursor to before the first element and updates any cached values.
     */
    void reset();

    /** Returns the element the curor is pointing to. */
    FreerailsSerializable getElement();

    /** Returns the index of the element the cursor is pointing
     * to.  The value returned is index you would need
     * to use in <code>World.get(KEY key, int index)</code> to
     * retrieve the same element as is returned by <code>getElement()</code>
     */
    int getIndex();

    /** Returns the number of the row where the cursor is.
     */
    int getRowNumber();

    /** Returns the number of rows */
    int size();

    /** Moves the cursor to the specified index. */
    void gotoIndex(int i);
}
