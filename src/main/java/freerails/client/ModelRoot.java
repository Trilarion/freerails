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
package freerails.client;

import freerails.controller.MoveExecutor;
import freerails.network.command.CommandToServer;

/**
 * Defines methods and constants that GUI classes can use to access shared data.
 */
public interface ModelRoot extends MoveExecutor {

    /**
     * @param message
     */
    void sendCommand(CommandToServer message);

    /**
     * @param property
     * @param value
     */
    void setProperty(ModelRootProperty property, Object value);

    /**
     * @param property
     * @return
     */
    Object getProperty(ModelRootProperty property);

}