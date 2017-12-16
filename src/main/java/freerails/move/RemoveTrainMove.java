/*
 * Created on 15-Apr-2003
 *
 */
package freerails.move;

import freerails.world.cargo.ImmutableCargoBundle;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.top.KEY;
import freerails.world.top.ReadOnlyWorld;
import freerails.world.train.ImmutableSchedule;
import freerails.world.train.TrainModel;

/**
 * This Move removes a train from the list of trains, and the corresponding
 * CargoBundle and Schedule.
 * 
 * @author Luke
 * 
 */
public class RemoveTrainMove extends CompositeMove {
    private static final long serialVersionUID = 3979265867567544114L;

    private RemoveTrainMove(Move[] moves) {
        super(moves);
    }

    public static RemoveTrainMove getInstance(int index, FreerailsPrincipal p,
            ReadOnlyWorld world) {
        TrainModel train = (TrainModel) world.get(p, KEY.TRAINS, index);
        int scheduleId = train.getScheduleID();
        ImmutableSchedule schedule = (ImmutableSchedule) world.get(p,
                KEY.TRAIN_SCHEDULES, scheduleId);
        int cargoBundleId = train.getCargoBundleID();
        ImmutableCargoBundle cargoBundle = (ImmutableCargoBundle) world.get(p,
                KEY.CARGO_BUNDLES, cargoBundleId);
        // TrainPositionOnMap position =
        // (TrainPositionOnMap)world.get(KEY.TRAIN_POSITIONS, index, p);
        Move removeTrain = new RemoveItemFromListMove(KEY.TRAINS, index, train,
                p);
        Move removeCargobundle = new RemoveItemFromListMove(KEY.CARGO_BUNDLES,
                cargoBundleId, cargoBundle, p);
        Move removeSchedule = new RemoveItemFromListMove(KEY.TRAIN_SCHEDULES,
                scheduleId, schedule, p);
        // Move removePosition = new RemoveItemFromListMove(KEY.TRAIN_POSITIONS,
        // index, position, p);

        return new RemoveTrainMove(new Move[] { removeTrain, removeCargobundle,
                removeSchedule /* , removePosition */
        });
    }
}