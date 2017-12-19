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
 * Created on 13-Oct-2004
 *
 */
package freerails.controller;

import freerails.move.Move;
import freerails.world.ReadOnlyWorld;

import java.io.Serializable;

/**
 * Defines a method that generates a move based on the state of the world
 * object. The state of a move is often a function of the state of the world
 * object and some other input.
 */
public interface PreMove extends Serializable {

    /**
     * @param w
     * @return
     */
    Move generateMove(ReadOnlyWorld w);
}