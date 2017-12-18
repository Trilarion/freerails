/*
 * Created on 22-May-2005
 *
 */
package freerails.world.train;

import junit.framework.TestCase;

/**
 *
 * @author jkeller1
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
