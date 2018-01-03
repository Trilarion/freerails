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
package freerails.controller;

/**
 * Thrown when a path cannot be found.
 */
public class PathNotFoundException extends Exception {
    private static final long serialVersionUID = 4121409601112717368L;

    /**
     * @param message message
     * @param cause   cause
     */
    public PathNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param arg0
     */
    public PathNotFoundException(String arg0) {
        super(arg0);
    }
}