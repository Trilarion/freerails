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

import freerails.move.Move;
import freerails.move.TimeTickMove;
import freerails.world.ReadOnlyWorld;

import java.io.ObjectStreamException;

/**
 * Generates a TimeTickMove.
 */
@freerails.util.InstanceControlled
public class TimeTickPreMove implements PreMove {

    /**
     *
     */
    public static final TimeTickPreMove INSTANCE = new TimeTickPreMove();
    private static final long serialVersionUID = 3690479125647208760L;

    private TimeTickPreMove() {

    }

    /**
     * @param w
     * @return
     */
    public Move generateMove(ReadOnlyWorld w) {
        return TimeTickMove.getMove(w);
    }

    private Object readResolve() throws ObjectStreamException {
        return INSTANCE;
    }
}