/*
 * Created on 15-Apr-2003
 *
 */
package jfreerails.move;

import jfreerails.world.accounts.AddItemTransaction;
import jfreerails.world.accounts.Transaction;
import jfreerails.world.common.Money;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.KEY;
import jfreerails.world.train.ImmutableSchedule;
import jfreerails.world.train.TrainModel;


/**
 * This CompositeMove adds a train to the train list, a train schedule to the schedule list and charges the player for it.
 * @author Luke
 *
 */
public class AddTrainMove extends CompositeMove {
    private static final long serialVersionUID = 4121972551066399792L;

	private AddTrainMove(Move[] moves) {
        super(moves);
    }

    public static AddTrainMove generateMove(int i, TrainModel train,
        Money price, ImmutableSchedule s, FreerailsPrincipal p) {
        Move m = new AddItemToListMove(KEY.TRAINS, i, train, p);
        Move m2 = new AddItemToListMove(KEY.TRAIN_SCHEDULES,
                train.getScheduleID(), s, p);
        Move m3 = new AddItemToListMove(KEY.TRAIN_POSITIONS, i, null, p);
        int quantity = 1;
        int engineType = train.getEngineType();
        Transaction transaction = new AddItemTransaction(Transaction.Category.TRAIN,
                engineType, quantity, new Money(-price.getAmount()));
        AddTransactionMove transactionMove = new AddTransactionMove(p,
                transaction);

        return new AddTrainMove(new Move[] {m, transactionMove, m2, m3});
    }
}