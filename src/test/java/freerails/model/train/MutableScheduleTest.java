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
package freerails.model.train;

import freerails.model.train.schedule.ImmutableSchedule;
import freerails.model.train.schedule.MutableSchedule;
import junit.framework.TestCase;

/**
 *
 */
public class MutableScheduleTest extends TestCase {

    /**
     *
     */
    public void test1() {
        TrainOrders trainOrders1 = new TrainOrders(0, null, false, false);
        TrainOrders trainOrders2 = new TrainOrders(1, null, false, false);

        MutableSchedule mutableSchedule = new MutableSchedule();
        mutableSchedule.addOrder(trainOrders1);
        mutableSchedule.addOrder(trainOrders2);

        int stationToGoto = mutableSchedule.getStationToGoto();
        assertEquals(0, stationToGoto);

        ImmutableSchedule immutableSchedule = mutableSchedule.toImmutableSchedule();
        assertEquals(0, immutableSchedule.getStationToGoto());
    }

}
