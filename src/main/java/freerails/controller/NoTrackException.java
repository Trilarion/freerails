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

package freerails.controller;

/**
 * throw this exception if a track is expected, but none was laid on the tile
 */
public class NoTrackException extends RuntimeException {

    /**
     *
     */
    public NoTrackException() {
        super();
    }

    /**
     * @param message message
     * @param cause   cause
     */
    public NoTrackException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message message
     */
    public NoTrackException(String message) {
        super(message);
    }

    /**
     * @param cause cause
     */
    public NoTrackException(Throwable cause) {
        super(cause);
    }

}
