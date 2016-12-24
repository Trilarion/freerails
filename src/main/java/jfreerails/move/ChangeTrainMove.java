/*
 * Created on 25-Aug-2003
 *
 */
package jfreerails.move;

import jfreerails.world.common.FreerailsSerializable;
import jfreerails.world.common.ImInts;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.KEY;
import jfreerails.world.train.TrainModel;

/**
 * This Move can change a train's engine and wagons.
 * 
 * @author Luke Lindsay
 * 
 */
public class ChangeTrainMove extends ChangeItemInListMove {
    private static final long serialVersionUID = 3257854272514242873L;

    private ChangeTrainMove(int index, FreerailsSerializable before,
            FreerailsSerializable after, FreerailsPrincipal p) {
        super(KEY.TRAINS, index, before, after, p);
    }

    public static ChangeTrainMove generateMove(int id, TrainModel before,
            int newEngine, ImInts newWagons, FreerailsPrincipal p) {
        TrainModel after = before.getNewInstance(newEngine, newWagons);

        return new ChangeTrainMove(id, before, after, p);
    }
}