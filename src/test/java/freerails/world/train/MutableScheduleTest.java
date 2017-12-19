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
 * Created on 22-May-2005
 *
 */
package freerails.world.train;

import junit.framework.TestCase;

/**
 *
 */
public class MutableScheduleTest extends TestCase {

    /**
     *
     */
    public void test1() {
        TrainOrdersModel order0 = new TrainOrdersModel(0, null, false, false);
        TrainOrdersModel order1 = new TrainOrdersModel(1, null, false, false);
        MutableSchedule s = new MutableSchedule();
        s.addOrder(order0);
        s.addOrder(order1);
        int station2Goto = s.getStationToGoto();
        assertEquals(0, station2Goto);
        // ImmutableSchedule is = s.toImmutableSchedule();
        //				
        // int i = is.getStationToGoto();
    }

}
