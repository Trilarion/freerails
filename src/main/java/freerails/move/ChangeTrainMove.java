/*
 * Created on 25-Aug-2003
 *
 */
package freerails.move;

import freerails.world.common.FreerailsSerializable;
import freerails.world.common.ImInts;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.top.KEY;
import freerails.world.train.TrainModel;

/**
 * This Move can change a train's engine and wagons.
 *
 * @author Luke Lindsay
 */
public class ChangeTrainMove extends ChangeItemInListMove {
    private static final long serialVersionUID = 3257854272514242873L;

    private ChangeTrainMove(int index, FreerailsSerializable before,
                            FreerailsSerializable after, FreerailsPrincipal p) {
        super(KEY.TRAINS, index, before, after, p);
    }

    /**
     *
     * @param id
     * @param before
     * @param newEngine
     * @param newWagons
     * @param p
     * @return
     */
    public static ChangeTrainMove generateMove(int id, TrainModel before,
                                               int newEngine, ImInts newWagons, FreerailsPrincipal p) {
        TrainModel after = before.getNewInstance(newEngine, newWagons);

        return new ChangeTrainMove(id, before, after, p);
    }
}