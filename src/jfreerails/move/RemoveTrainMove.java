/*
 * Created on 15-Apr-2003
 *
 */
package jfreerails.move;

import jfreerails.world.cargo.ImmutableCargoBundle;
import jfreerails.world.player.FreerailsPrincipal;
import jfreerails.world.top.KEY;
import jfreerails.world.top.ReadOnlyWorld;
import jfreerails.world.train.ImmutableSchedule;
import jfreerails.world.train.TrainModel;


/**
 * This Move removes a train from the list of trains, and the corresponding CargoBundle and Schedule.
 * @author Luke
 *
 */
public class RemoveTrainMove extends CompositeMove {
    private RemoveTrainMove(Move[] moves) {
        super(moves);
    }

    public static RemoveTrainMove getInstance(int index, FreerailsPrincipal p,
        ReadOnlyWorld world) {
        TrainModel train = (TrainModel)world.get(KEY.TRAINS, index, p);
        int scheduleId = train.getScheduleID();
        ImmutableSchedule schedule = (ImmutableSchedule)world.get(KEY.TRAIN_SCHEDULES,
                scheduleId, p);
        int cargoBundleId = train.getCargoBundleNumber();
        ImmutableCargoBundle cargoBundle = (ImmutableCargoBundle)world.get(KEY.CARGO_BUNDLES,
                cargoBundleId, p);
        Move removeTrain = new RemoveItemFromListMove(KEY.TRAINS, index, train,
                p);
        Move removeCargobundle = new RemoveItemFromListMove(KEY.CARGO_BUNDLES,
                cargoBundleId, cargoBundle, p);
        Move removeSchedule = new RemoveItemFromListMove(KEY.TRAIN_SCHEDULES,
                scheduleId, schedule, p);

        return new RemoveTrainMove(new Move[] {
                removeTrain, removeCargobundle, removeSchedule
            });
    }
}