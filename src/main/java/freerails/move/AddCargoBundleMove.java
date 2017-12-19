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
 * Created on 26-May-2003
 *
 */
package freerails.move;

import freerails.world.cargo.ImmutableCargoBundle;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.top.KEY;

/**
 * This Move adds a cargo bundle to the cargo bundle list.
 */
public class AddCargoBundleMove extends AddItemToListMove {
    private static final long serialVersionUID = 3257288049795674934L;

    /**
     * @param i
     * @param item
     * @param p
     */
    public AddCargoBundleMove(int i, ImmutableCargoBundle item,
                              FreerailsPrincipal p) {
        super(KEY.CARGO_BUNDLES, i, item, p);
    }
}