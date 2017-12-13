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
 * Created on 26-May-2003
 *
 */
package org.railz.move;

import org.railz.world.cargo.CargoBatch;
import org.railz.world.cargo.CargoBundle;
import org.railz.world.cargo.CargoBundleImpl;
import org.railz.world.top.KEY;


/**
 * @author Luke
 *
 */
public class ChangeCargoBundleMoveTest extends AbstractMoveTestCase {
    public void testMove() {
        CargoBundle before;
        CargoBundle after;
        before = new CargoBundleImpl();
        after = new CargoBundleImpl();
        before.setAmount(new CargoBatch(1, 2, 3, 4, 0), 5);
        after.setAmount(new CargoBatch(1, 2, 3, 4, 0), 8);

        Move m = new ChangeCargoBundleMove(before, after, 0);
        assertEqualsSurvivesSerialisation(m);

        assertTryMoveFails(m);
        assertTryUndoMoveFails(m);
        getWorld().add(KEY.CARGO_BUNDLES, before);
    }
}
