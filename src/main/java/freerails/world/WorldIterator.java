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
package freerails.world;

import java.io.Serializable;
import java.util.NoSuchElementException;

/**
 * This interface lets the caller access the results of a search in the
 * game world. It is similar in concept to {@code java.sql.ResultSet}.
 */

public interface WorldIterator {

    int BEFORE_FIRST = -1;

    /**
     * Moves the cursor down one row from its current position.
     */
    boolean next();

    /**
     * Moves the cursor up one row from its current position.
     */
    boolean previous();

    /**
     * Moves the cursor to before the first element and updates any cached
     * values.
     */
    void reset();

    /**
     * Returns the element the cursor is pointing to.
     */
    Serializable getElement();

    /**
     * Returns the index of the element the cursor is pointing to. The value
     * returned is index you would need to use in
     * {@code World.get(KEY key, int index)} to retrieve the same element
     * as is returned by {@code getElement()}
     */
    int getIndex();

    /**
     * Returns the number of the row where the cursor is (the first row is 0).
     */
    int getRowID();

    /**
     * Returns the number of rows.
     */
    int size();

    /**
     * Moves the cursor to the specified index.
     *
     * @throws NoSuchElementException if index out of range
     */
    void gotoIndex(int i);

    /**
     * Moves the cursor to the specified index.
     */
    void gotoRow(int row);

    /**
     * Returns the number of the row where the cursor is (the first row is 1).
     */
    int getNaturalNumber();
}