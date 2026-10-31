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

import freerails.move.MoveExecutor;
import freerails.network.command.CommandToServer;

/**
 * Defines methods and constants that GUI classes can use to access shared data.
 */
public interface ModelRoot extends MoveExecutor {

    /**
     * Performs the send command operation.
     *
     * @param message the message parameter
     */
    void sendCommand(CommandToServer message);

    /**
     * Sets the property.
     *
     * @param property the client property to access
     * @param value the value to use
     */
    void setProperty(ModelRootProperty property, Object value);

    /**
     * Returns the property.
     *
     * @param property the client property to access
     * @return the property produced by this method
     */
    Object getProperty(ModelRootProperty property);

}