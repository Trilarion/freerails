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

package freerails.model.track;

import java.io.Serial;

/**
 * Throw this exception if a track was expected, but none was laid on a terrain tile.
 */
public class NoTrackException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -4345207670990590768L;

    /**
     * Creates an exception describing the missing track.
     *
     * @param message detail describing where track was expected
     */
    public NoTrackException(String message) {
        super(message);
    }

}
