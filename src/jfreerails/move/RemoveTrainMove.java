/*
 * Created on 15-Apr-2003
 *
 */
package jfreerails.move;

import jfreerails.world.top.KEY;
import jfreerails.world.train.TrainModel;
import jfreerails.world.player.FreerailsPrincipal;

/**
 * This Move removes a train from the list of trains.
 * @author Luke
 *
 */
public class RemoveTrainMove extends RemoveItemFromListMove {
    public RemoveTrainMove(int index, FreerailsPrincipal p, TrainModel train) {
        super(KEY.TRAINS, index, train, p);
    }
}
