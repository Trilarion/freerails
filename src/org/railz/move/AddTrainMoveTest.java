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
 * Created on 25-Aug-2003
 *
  */
package org.railz.move;

import org.railz.world.common.*;
import org.railz.world.top.*;
import org.railz.world.train.ImmutableSchedule;
import org.railz.world.train.TrainModel;
import org.railz.world.train.TrainOrdersModel;

/**
 * @author Luke Lindsay
 *
 */
public class AddTrainMoveTest extends AbstractMoveTestCase {
    public void testMove() {
	GameTime now = (GameTime) getWorld().get(ITEM.TIME,
		testPlayer.getPrincipal());
        TrainModel train = new TrainModel(0, new int[] {0, 1, 0}, 0,
		now);

	TrainOrdersModel orders = new TrainOrdersModel(new
		ObjectKey(KEY.STATIONS, testPlayer.getPrincipal(), 0),
		new int[] {1, 2, 3}, true, true, true);
	
        ImmutableSchedule schedule = new ImmutableSchedule
	    (new TrainOrdersModel[] { orders, orders, orders });
        long price = 100;
	AddTrainMove move = AddTrainMove.generateMove(getWorld(), 0, train,
		price, schedule, testPlayer.getPrincipal());
        assertTryMoveIsOk(move);
        assertEqualsSurvivesSerialisation(move);
    }
}
