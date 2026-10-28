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

package freerails.model.train;

/** Lifecycle and update states used to describe a train's current activity. */
public enum TrainState {

    /** Train is stopped at a station. */
    STOPPED_AT_STATION,
    /** Train is ready to move or is moving. */
    READY,
    /** Train is stopped while waiting for enough cargo to fill its consist. */
    WAITING_FOR_FULL_LOAD,
    /** Train is stopped at a signal. */
    STOPPED_AT_SIGNAL,
    /** Train has been involved in a crash. */
    CRASHED,
    /** Train requires its next server-side update. */
    NEEDS_UPDATING
}
