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

/*
 *
 */
package freerails.client;

/**
 * Receives notifications when a property of the client model root changes.
 */
public interface ModelRootListener {

    /**
     * Performs the property change operation.
     *
     * @param modelRootProperty the model root property parameter
     * @param oldValue the old value parameter
     * @param newValue the new value parameter
     */
    void propertyChange(ModelRootProperty modelRootProperty, Object oldValue, Object newValue);

}
