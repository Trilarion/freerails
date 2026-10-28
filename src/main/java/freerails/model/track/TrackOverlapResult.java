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

import java.util.Objects;

/**
 * The outcome of checking whether a station overlaps another station.
 *
 * @param isSuccess whether the proposed station placement is valid
 * @param message explanation when the placement is invalid, or {@code null}
 *                when it is valid
 */
public record TrackOverlapResult(boolean isSuccess, String message) {

    /**
     * Validates the consistency between the success flag and failure message.
     *
     * @param isSuccess whether the overlap check succeeded
     * @param message failure explanation, or {@code null} on success
     * @throws IllegalArgumentException if a successful result has a message
     * @throws NullPointerException if a failed result has a {@code null} message
     */
    public TrackOverlapResult {
        if (isSuccess && message != null) {
            throw new IllegalArgumentException("A successful result cannot have a failure message.");
        }
        if (!isSuccess) {
            Objects.requireNonNull(message, "A failed result must have a message.");
        }
    }

    /**
     * Creates a successful overlap-check result.
     *
     * @return successful result
     */
    public static TrackOverlapResult success() {
        return new TrackOverlapResult(true, null);
    }

    /**
     * Creates a failed overlap-check result.
     *
     * @param message explanation of the overlap
     * @return failed result
     * @throws NullPointerException if {@code message} is null
     */
    public static TrackOverlapResult failure(String message) {
        return new TrackOverlapResult(false, Objects.requireNonNull(message, "message"));
    }
}
