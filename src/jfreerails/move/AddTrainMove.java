/*
 * Created on 15-Apr-2003
 *
 */
package jfreerails.move;

import jfreerails.world.accounts.AddItemTransaction;
import jfreerails.world.common.GameTime;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ITEM;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.train.ImmutableSchedule;
import jfreerails.world.train.TrainModel;
import jfreerails.world.player.FreerailsPrincipal;

/**
 * This CompositeMove adds a train to the train list, a train schedule to the
 * schedule list and charges the player for it.
 * @author Luke
 *
 */
public class AddTrainMove extends CompositeMove {
    private AddTrainMove(Move[] moves) {
        super(moves);
    }

    public static AddTrainMove generateMove(ReadOnlyWorld w, int i, TrainModel
	    train, long price, ImmutableSchedule s, FreerailsPrincipal p) {
        Move m = new AddItemToListMove(KEY.TRAINS, i, train, p);
        Move m2 = new AddItemToListMove(KEY.TRAIN_SCHEDULES,
                train.getScheduleID(), s);
	GameTime now = (GameTime) w.get(ITEM.TIME, p);
	AddItemTransaction t = new AddItemTransaction(now,
		AddItemTransaction.ROLLING_STOCK, 0, 1, -price);
        AddTransactionMove transactionMove = new AddTransactionMove(0,
                t, p);
	

        return new AddTrainMove(new Move[] {m, transactionMove, m2});
    }
}
