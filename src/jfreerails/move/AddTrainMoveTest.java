/*
 * Created on 25-Aug-2003
 *
  */
package jfreerails.move;

import jfreerails.world.top.KEY;
import jfreerails.world.top.ObjectKey;
import jfreerails.world.train.ImmutableSchedule;
import jfreerails.world.train.TrainModel;
import jfreerails.world.train.TrainOrdersModel;

/**
 * @author Luke Lindsay
 *
 */
public class AddTrainMoveTest extends AbstractMoveTestCase {
    public void testMove() {
        TrainModel train = new TrainModel(0, new int[] {0, 1, 0}, null, 0);

	TrainOrdersModel orders = new TrainOrdersModel(new
		ObjectKey(KEY.STATIONS, testPlayer.getPrincipal(), 0),
		new int[] {1, 2, 3}, true);
	
        ImmutableSchedule schedule = new ImmutableSchedule(new TrainOrdersModel[] {
                    orders, orders, orders
                }, 1, true);
        long price = 100;
	AddTrainMove move = AddTrainMove.generateMove(getWorld(), 0, train,
		price, schedule, testPlayer.getPrincipal());
        assertTryMoveIsOk(move);
        assertEqualsSurvivesSerialisation(move);
    }
}
