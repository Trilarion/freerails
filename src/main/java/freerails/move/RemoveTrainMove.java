/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 *
 */
package freerails.move;

import freerails.world.KEY;
import freerails.world.ReadOnlyWorld;
import freerails.world.cargo.ImmutableCargoBundle;
import freerails.world.player.FreerailsPrincipal;
import freerails.world.train.ImmutableSchedule;
import freerails.world.train.TrainModel;

/**
 * This Move removes a train from the list of trains, and the corresponding
 * CargoBundle and Schedule.
 */
public class RemoveTrainMove extends CompositeMove {
    private static final long serialVersionUID = 3979265867567544114L;

    private RemoveTrainMove(Move[] moves) {
        super(moves);
    }

    /**
     * @param index
     * @param p
     * @param world
     * @return
     */
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

        return new RemoveTrainMove(new Move[]{removeTrain, removeCargobundle,
                removeSchedule /* , removePosition */
        });
    }
}