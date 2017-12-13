/*
 * Copyright (C) 2003 Luke Lindsay
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

/*
 * Created on 24-May-2003
 *
 */
package org.railz.move;

import org.railz.world.cargo.CargoBundle;
import org.railz.world.top.KEY;


/**
 * This {@link Move} changes a cargo bundle (cargo bundles are used to represent
 * the cargo carried by trains and the cargo waiting at stations).
 * @author Luke
 *
 */
public class ChangeCargoBundleMove extends ChangeItemInListMove {
    public ChangeCargoBundleMove(CargoBundle before, CargoBundle after,
        int bundleNumber) {
        super(KEY.CARGO_BUNDLES, bundleNumber, before, after);
    }
}
