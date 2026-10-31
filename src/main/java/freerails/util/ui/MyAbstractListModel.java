/*
 * FreeRails
 * Copyright (C) 2000-2026 The FreeRails Team
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

package freerails.util.ui;

import javax.swing.*;
import java.io.Serial;

/**
 * List model exposing the fixed informational message used when no players
 * are logged on.
 */
public class MyAbstractListModel extends AbstractListModel<String> {

    @Serial
    private static final long serialVersionUID = -7077093078891444168L;
    private final String[] strings = {"No players are logged on!"};

    /**
     * Returns the number of entries in this model.
     *
     * @return the fixed list size
     */
    @Override
    public int getSize() {
        return strings.length;
    }

    /**
     * Returns the message at the requested list index.
     *
     * @param index entry index
     * @return the informational message
     * @throws ArrayIndexOutOfBoundsException if the index is outside the model
     */
    @Override
    public String getElementAt(int index) {
        return strings[index];
    }
}
