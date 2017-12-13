/*
 * Created on 25-Aug-2003
 *
  */
package jfreerails.move;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.top.KEY;
import jfreerails.world.train.TrainModel;


/**
 * This Move can change a train's engine and wagons.
 *
 * @author Luke Lindsay
 *
 */
public class ChangeTrainMove extends ChangeItemInListMove {
    private ChangeTrainMove(int index, FreerailsSerializable before,
        FreerailsSerializable after) {
        super(KEY.TRAINS, index, before, after);
    }

    public static ChangeTrainMove generateMove(int id, TrainModel before,
        int newEngine, int[] newWagons) {
        TrainModel after = before.getNewInstance(newEngine, newWagons);

        return new ChangeTrainMove(id, before, after);
    }
}