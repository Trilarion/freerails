/*
 * Created on 25-Aug-2003
 *
  */
package jfreerails.move;

import jfreerails.world.common.Money;
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
        TrainOrdersModel orders = new TrainOrdersModel(0, new int[] {1, 2, 3},
                true);
        ImmutableSchedule schedule = new ImmutableSchedule(new TrainOrdersModel[] {
                    orders, orders, orders
                }, 1, true);
        Money price = new Money(100);
	AddTrainMove move = AddTrainMove.generateMove(0, train, price,
		schedule, testPlayer.getPrincipal());
        assertTryMoveIsOk(move);
        assertEqualsSurvivesSerialisation(move);
    }
}
